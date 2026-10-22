package com.veil.app

import android.app.UiAutomation
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Configurator
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.veil.app.services.CommitmentStore
import com.veil.app.services.MonitoringRuntime
import com.veil.app.services.VeilMonitoringService
import java.io.File
import java.util.regex.Pattern
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Isolated emulator only: genuine consent, genuine frame/model/policy/overlay. No fabricated score. */
@RunWith(AndroidJUnit4::class)
class CaptureJourneyTest {
    @Test fun realConsentedCaptureToSyntheticFalsePositiveAndSafeExit() {
        assumeTrue("Never changes accessibility settings on physical hardware", isEmulator())
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val config = Configurator.getInstance()
        val originalFlags = config.uiAutomationFlags
        config.setUiAutomationFlags(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES)
        val device = UiDevice.getInstance(instrumentation)
        val evidence = File(context.getExternalFilesDir(null), "capture-evidence").apply { mkdirs() }
        fun shell(command: String): String = device.executeShellCommand(command).trim()
        val previousServices = shell("settings get secure enabled_accessibility_services")
        val previousEnabled = shell("settings get secure accessibility_enabled")
        val store = CommitmentStore.get(context)
        val started = SystemClock.elapsedRealtime()
        val report = JSONObject().put("purpose", "synthetic numeric integration only; no semantic accuracy claim")
            .put("deviceModel", Build.MODEL).put("sdk", Build.VERSION.SDK_INT)
        fun record(name: String) { report.put(name + "ElapsedMs", SystemClock.elapsedRealtime() - started) }
        try {
            context.stopService(Intent(context, VeilMonitoringService::class.java))
            await("previous capture stops", 10_000) { !MonitoringRuntime.isRunning.value }
            store.clearAll()
            // Fresh local rules/history, no injected detections, no fabricated accessibility callback.
            instrumentation.startActivitySync(Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            instrumentation.waitForIdleSync()
            device.waitForIdle(3_000)
            assertTrue(device.wait(Until.hasObject(By.pkg(context.packageName).depth(0)), 20_000))
            if (store.accessibilityConnected.value) {
                clickText(device, "Privacy")
                clickText(device, "Review foreground awareness")
            } else clickText(device, "Set up foreground awareness")
            clickText(device, "I agree — open Settings")
            assertTrue("Actual Android accessibility Settings opens",
                device.wait(Until.hasObject(By.pkg("com.android.settings").depth(0)), 15_000))
            val component = "com.veil.app/com.veil.app.services.VeilAccessibilityService"
            val configured = previousServices.takeUnless { it == "null" || it.isBlank() }
                ?.split(':')?.toMutableSet() ?: mutableSetOf()
            configured.add(component)
            // Emulator provisioning only. Android actually binds the production service.
            shell("settings put secure enabled_accessibility_services " + settingValue(configured.joinToString(":")))
            shell("settings put secure accessibility_enabled 1")
            await("real accessibility connection", 20_000) { store.accessibilityConnected.value }
            record("accessibilityConnected")
            instrumentation.startActivitySync(Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
            instrumentation.waitForIdleSync()
            device.waitForIdle(3_000)
            clickText(device, "Home")
            clickText(device, "Review and start screen sharing", 60_000)
            clickText(device, "Continue to Android consent")
            assertTrue("Real Android sharing consent must appear",
                device.wait(Until.hasObject(By.pkg("com.android.systemui").depth(0)), 15_000))
            device.takeScreenshot(File(evidence, "android-consent.png"))
            device.dumpWindowHierarchy(File(evidence, "android-consent.xml"))
            val consent = device.wait(Until.findObject(By.res("android", "button1").pkg("com.android.systemui")), 5_000)
                ?: device.wait(Until.findObject(By.res("com.android.systemui", "start_button")), 5_000)
                ?: throw AssertionError("Android sharing consent confirmation not found; see hierarchy")
            assertTrue("Android consent confirmation must be enabled", consent.isEnabled)
            consent.click()
            await("consented capture running", 20_000) { MonitoringRuntime.isRunning.value }
            await("native model initialized and projection started", 30_000) {
                MonitoringRuntime.status.value.startsWith("Screen sharing active")
            }
            record("projectionStarted")
            assertEquals("No injected or premature intervention", 0, store.state.value.events.count { it.kind == "AI_INTERVENTION" })
            // Explicit exported fixture activity, installed as its own test-only APK by CI.
            context.startActivity(Intent().setComponent(ComponentName(FIXTURE_PACKAGE, "com.veil.fixture.FixtureActivity"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            await("real captured frames must trigger unchanged numeric policy", 60_000) {
                store.state.value.cooldownPackage == FIXTURE_PACKAGE && store.state.value.events.any { it.kind == "AI_INTERVENTION" }
            }
            record("actualCaptureModelIntervention")
            assertTrue(store.state.value.cooldownRemaining(SystemClock.elapsedRealtime(), store.bootCount()) > 0)
            assertNotNull(store.blockingReason(FIXTURE_PACKAGE))
            clickText(device, "Leave this app", 15_000, click = false)
            device.takeScreenshot(File(evidence, "real-detection-overlay.png"))
            device.dumpWindowHierarchy(File(evidence, "real-detection-overlay.xml"))
            clickText(device, "Leave this app")
            await("safe Home removes intervention overlay", 10_000) {
                !device.hasObject(textSelector("Leave this app")) && MonitoringRuntime.foregroundPackage.value == null
            }
            record("safeHomeExit")
            assertNull("System Settings is never blocked", store.blockingReason("com.android.settings"))
            // Returning to this app demonstrates persisted app-specific cooldown, not a global lock.
            context.startActivity(Intent().setComponent(ComponentName(FIXTURE_PACKAGE, "com.veil.fixture.FixtureActivity"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            clickText(device, "Incorrect detection — stop guardian", 15_000)
            await("false-positive dismissal clears cooldown and ends sharing", 10_000) {
                store.state.value.cooldownPackage.isBlank() && !MonitoringRuntime.isRunning.value
            }
            assertNull(store.blockingReason(FIXTURE_PACKAGE))
            assertTrue(store.state.value.events.any { it.kind == "FALSE_POSITIVE" })
            val eventCount = store.state.value.events.count { it.kind == "AI_INTERVENTION" }
            SystemClock.sleep(2_500)
            assertEquals("Stopped capture must not immediately re-block the synthetic fixture", eventCount,
                store.state.value.events.count { it.kind == "AI_INTERVENTION" })
            assertFalse(device.hasObject(textSelector("Leave this app")))
            record("falsePositiveExit")

            // System B uses the actual bound service while AI is off. Only rule setup uses the
            // store API; real monotonic time, service ticks, overlay and exits remain unmodified.
            assertTrue("Accessibility remains actually bound after stopping AI", store.accessibilityConnected.value)
            val focusStarted = SystemClock.elapsedRealtime()
            store.startFocus(setOf(FIXTURE_PACKAGE), 1)
            clickText(device, "Leave this app", 15_000, click = false)
            assertFalse("Focus must not restart screen sharing", MonitoringRuntime.isRunning.value)
            assertNotNull("The chosen fixture app is restricted during focus", store.blockingReason(FIXTURE_PACKAGE))
            device.takeScreenshot(File(evidence, "focus-without-ai-overlay.png"))
            record("focusWithoutAiOverlay")
            await("one-minute focus expires through actual service ticks", 75_000) {
                store.state.value.focusPackages.isEmpty() &&
                    store.state.value.events.any { it.kind == "FOCUS_COMPLETED" } &&
                    !device.hasObject(textSelector("Leave this app"))
            }
            val actualFocusElapsed = SystemClock.elapsedRealtime() - focusStarted
            assertTrue("Real focus must last at least 60 seconds; no deadline mutation", actualFocusElapsed >= 60_000)
            assertNull(store.blockingReason(FIXTURE_PACKAGE))
            assertFalse(MonitoringRuntime.isRunning.value)
            assertTrue(store.accessibilityConnected.value)
            report.put("actualOneMinuteFocusElapsedMs", actualFocusElapsed)
            record("focusActuallyExpired")

            store.startFocus(setOf(FIXTURE_PACKAGE), 1)
            clickText(device, "Emergency exit — end restrictions", 15_000)
            await("real emergency control clears focus and overlay while AI stays off", 10_000) {
                store.state.value.focusPackages.isEmpty() && store.state.value.focusEndElapsed == 0L &&
                    !MonitoringRuntime.isRunning.value && !device.hasObject(textSelector("Leave this app"))
            }
            assertTrue(store.state.value.events.any { it.kind == "FOCUS_ENDED_EARLY" })
            assertNull(store.blockingReason(FIXTURE_PACKAGE))
            assertEquals("Focus timer and emergency exit must not produce AI events", eventCount,
                store.state.value.events.count { it.kind == "AI_INTERVENTION" })
            record("focusEmergencyExit")
            report.put("aiInterventions", eventCount).put("passed", true)
        } catch (error: Throwable) {
            report.put("passed", false).put("failure", error.toString()).put("monitoringStatus", MonitoringRuntime.status.value)
            runCatching { File(evidence, "accessibility-diagnostics.txt").writeText(
                shell("settings get secure enabled_accessibility_services") + "\n" + shell("dumpsys accessibility")) }
            runCatching { device.takeScreenshot(File(evidence, "failure.png")) }
            runCatching { device.dumpWindowHierarchy(File(evidence, "failure.xml")) }
            throw error
        } finally {
            context.stopService(Intent(context, VeilMonitoringService::class.java))
            store.clearAll()
            try {
                restoreSetting(device, "enabled_accessibility_services", previousServices)
                restoreSetting(device, "accessibility_enabled", previousEnabled)
                if (!previousServices.contains("com.veil.app.services.VeilAccessibilityService")) {
                    await("test-only accessibility binding restored", 10_000) { !store.accessibilityConnected.value }
                }
            } catch (cleanupError: Throwable) {
                report.put("passed", false).put("cleanupFailure", cleanupError.toString())
                throw cleanupError
            } finally {
                config.setUiAutomationFlags(originalFlags)
                File(evidence, "capture-journey.json").writeText(report.toString(2))
                // UTP uninstalls the target after tests. Preserve only this synthetic test
                // evidence via emulator shell before app-private external files are removed.
                shell("mkdir -p /sdcard/Download/veil-capture-evidence")
                shell("cp -R " + evidence.absolutePath + "/. /sdcard/Download/veil-capture-evidence/")
            }
        }
    }

    private fun clickText(device: UiDevice, text: String, timeout: Long = 15_000, click: Boolean = true) {
        val deadline = SystemClock.elapsedRealtime() + timeout
        while (SystemClock.elapsedRealtime() < deadline) {
            device.waitForIdle(2_000)
            val node = device.findObject(textSelector(text).enabled(true))
            if (node != null) {
                if (click) {
                    // Scroll flings/recreated activities can move a previously found text node.
                    // Re-resolve after idle and click its actual clickable Compose/native parent.
                    var control = node
                    while (!control.isClickable && control.parent != null) control = control.parent!!
                    control.click()
                    device.waitForIdle(2_000)
                }
                return
            }
            // Scroll only the Veil app; never blindly swipe Android's consent/settings UI.
            if (device.currentPackageName == "com.veil.app") {
                device.swipe(device.displayWidth / 2, device.displayHeight * 3 / 4,
                    device.displayWidth / 2, device.displayHeight / 3, 15)
            }
            SystemClock.sleep(200)
        }
        throw AssertionError("Visible enabled control not found: $text")
    }

    private fun await(label: String, timeout: Long, condition: () -> Boolean) {
        val deadline = SystemClock.elapsedRealtime() + timeout
        while (SystemClock.elapsedRealtime() < deadline) {
            if (condition()) return
            SystemClock.sleep(100)
        }
        throw AssertionError("Timed out waiting for $label; status=${MonitoringRuntime.status.value}")
    }

    private fun restoreSetting(device: UiDevice, key: String, previous: String) {
        if (previous == "null" || previous.isBlank()) device.executeShellCommand("settings delete secure $key")
        else device.executeShellCommand("settings put secure $key " + settingValue(previous))
    }

    // UiAutomation executes argv directly; shell quotes become literal setting characters.
    private fun settingValue(value: String): String {
        require(value.matches(Regex("[A-Za-z0-9_./:$-]+"))) { "Unexpected emulator setting value" }
        return value
    }
    private fun textSelector(text: String) = By.text(Pattern.compile(Pattern.quote(text),
        Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE))
    private fun isEmulator() = Build.HARDWARE in setOf("ranchu", "goldfish") ||
        Build.FINGERPRINT.startsWith("generic") || Build.MODEL.contains("sdk_gphone")

    companion object { private const val FIXTURE_PACKAGE = "com.veil.fixture" }
}
