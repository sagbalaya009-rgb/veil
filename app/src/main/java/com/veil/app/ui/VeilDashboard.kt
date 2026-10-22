package com.veil.app.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionConfig
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import com.veil.app.core.recovery.RecoveryActivity
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.veil.app.services.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Date

private data class AppChoice(val label: String, val pkg: String)

@Composable
fun VeilDashboard(legacyRecovery: @Composable () -> Unit, recoveryRequest: Int = 0) {
    val context = LocalContext.current
    val store = remember { CommitmentStore.get(context) }
    val state by store.state.collectAsStateWithLifecycle()
    val feedback = remember { FeedbackStore.get(context) }
    val recovery = remember { RecoveryStore.get(context) }
    val recoveryState by recovery.state.collectAsStateWithLifecycle()
    val health by MonitoringRuntime.health.collectAsStateWithLifecycle()
    var appSearch by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf("") }
    val exportScope = rememberCoroutineScope()
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        if (uri != null) exportScope.launch {
            val saved = withContext(Dispatchers.IO) {
                runCatching {
                    val stream = checkNotNull(context.contentResolver.openOutputStream(uri)) { "No writable document stream" }
                    stream.bufferedWriter().use { it.write(MonitoringRuntime.diagnosticText(context)) }
                }.isSuccess
            }
            message = if (saved) "Technical diagnostics saved." else "Diagnostics could not be saved. Choose another location and try again."
        }
    }
    val connected by store.accessibilityConnected.collectAsStateWithLifecycle()
    val monitoring by MonitoringRuntime.status.collectAsStateWithLifecycle()
    val running by MonitoringRuntime.isRunning.collectAsStateWithLifecycle()
    var consentPending by remember { mutableStateOf(false) }
    var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    var tab by rememberSaveable { mutableStateOf("Home") }
    var intro by rememberSaveable { mutableIntStateOf(0) }
    var disclosure by remember { mutableStateOf(false) }
    var captureDisclosure by remember { mutableStateOf(false) }
    var legacy by rememberSaveable { mutableStateOf(false) }
    var modelStatus by remember { mutableStateOf("Checking local model…") }
    var modelAvailable by remember { mutableStateOf(false) }
    var apps by remember { mutableStateOf<List<AppChoice>>(emptyList()) }
    var loadingApps by remember { mutableStateOf(true) }
    var selected by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var minutes by rememberSaveable { mutableStateOf("25") }
    var delayMinutes by rememberSaveable { mutableStateOf("0") }
    var reflection by remember { mutableStateOf("") }
    var breathing by rememberSaveable { mutableStateOf(false) }
    var breathSeconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(recoveryRequest) {
        if (recoveryRequest > 0) { legacy = false; tab = "Reset" }
    }
    val view = LocalView.current
    DisposableEffect(view, breathing) {
        val previous = view.keepScreenOn
        if (breathing) view.keepScreenOn = true
        onDispose { view.keepScreenOn = previous }
    }
    val capture = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        consentPending = false
        if (result.resultCode == Activity.RESULT_OK && result.data != null && !MonitoringRuntime.isRunning.value) {
            runCatching {
                ContextCompat.startForegroundService(context, Intent(context, VeilMonitoringService::class.java)
                    .setAction(VeilMonitoringService.ACTION_START)
                    .putExtra(VeilMonitoringService.RESULT_CODE, result.resultCode)
                    .putExtra(VeilMonitoringService.RESULT_DATA, result.data))
            }.onFailure { message = "Android could not start capture. Open Veil and try again." }
        } else message = "Screen sharing request dismissed. Current status is shown on Home."
    }
    val notification = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        message = if (granted) "Status notifications enabled." else "Notifications denied. Android's active capture indicator still applies."
    }
    LaunchedEffect(Unit) {
        while (true) { store.tick(); now = SystemClock.elapsedRealtime(); delay(1000) }
    }
    LaunchedEffect(Unit) {
        val result = withContext(Dispatchers.Default) {
            when (val initialized = TensorFlowLiteInitializer().initialize(context)) {
                is ClassifierInitialization.Ready -> { initialized.classifier.close(); true to "Local model loaded. Screening is experimental; accuracy on phone feeds is unvalidated." }
                is ClassifierInitialization.MissingModel -> false to "Local model missing. AI monitoring unavailable."
                is ClassifierInitialization.Failed -> false to "Model unavailable: ${initialized.reason}"
            }
        }
        modelAvailable = result.first; modelStatus = result.second
    }
    LaunchedEffect(Unit) {
        apps = withContext(Dispatchers.IO) {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            context.packageManager.queryIntentActivities(intent, 0)
                .map { AppChoice(it.loadLabel(context.packageManager).toString(), it.activityInfo.packageName) }
                .filterNot { isEssentialPackage(context, it.pkg) }.distinctBy { it.pkg }.sortedBy { it.label.lowercase() }
        }
        loadingApps = false
    }
    LaunchedEffect(breathing) {
        if (breathing) {
            breathSeconds = 0
            while (breathSeconds < 60) { delay(1000); breathSeconds++ }
            breathing = false; store.recoveryComplete(); recovery.completeActivity(RecoveryActivity.BREATHING); message = "One minute of breathing complete."
        }
    }
    val scheme = if (state.dark) darkColorScheme(primary = Color(0xFFB8A4FF), secondary = Color(0xFF68D5BB), background = Color(0xFF10131B), surface = Color(0xFF181D29), surfaceVariant = Color(0xFF242A38))
        else lightColorScheme(primary = Color(0xFF544282), secondary = Color(0xFF226D5D), background = Color(0xFFF2F0EA), surface = Color(0xFFFAF8F3), surfaceVariant = Color(0xFFE6E0EF))
    MaterialTheme(colorScheme = scheme) {
        Surface(Modifier.fillMaxSize(), color = scheme.background) {
            if (legacy) {
                Column(Modifier.systemBarsPadding()) {
                    TextButton(onClick = { legacy = false }) { Text("Back to dashboard") }
                    Box(Modifier.weight(1f)) { legacyRecovery() }
                }
            } else {
                Scaffold(containerColor = scheme.background, bottomBar = {
                    if (state.onboarded) NavigationBar {
                        listOf("Home", "Focus", "Reset", "History", "Privacy").forEach { name ->
                            NavigationBarItem(selected = tab == name, onClick = { tab = name },
                                icon = { Text(when (name) { "Home" -> "◌"; "Focus" -> "◎"; "Reset" -> "≈"; "History" -> "≡"; else -> "◇" }) }, label = { Text(navLabel(name)) })
                        }
                    }
                }) { padding ->
                    LazyColumn(Modifier.fillMaxSize().padding(padding).systemBarsPadding(),
                        contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("V / VEIL", style = MaterialTheme.typography.labelLarge, color = scheme.primary, fontWeight = FontWeight.Bold)
                            Text("ON YOUR TERMS", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                        } }
                        if (!state.onboarded) {
                            item {
                                Headline(listOf("Make room for what matters.", "Your screen stays yours.", "Protection, by your choice.")[intro])
                                Text(listOf("Create a focus commitment. Pause unwanted content. Return to your goals with a calmer mind.",
                                    "Screen frames stay in memory for local inference. No screenshot archive, account, cloud moderation or analytics. Protection history contains event types and times only. Optional Recovery records are stored separately with encryption on this device.",
                                    "Screen sharing needs Android consent each session. Foreground awareness needs optional accessibility access. You can revoke permissions, stop sharing or uninstall at any time. Veil cannot guarantee blocking or perfect classification.")[intro])
                            }
                            item { Button(onClick = { if (intro < 2) intro++ else store.onboard() }) { Text(if (intro < 2) "Continue" else "Open Veil") } }
                        } else {
                            if (message.isNotBlank()) item { ElevatedCard { Column(Modifier.padding(16.dp)) { Text(message); TextButton(onClick = { message = "" }) { Text("Dismiss") } } } }
                            when (tab) {
                                "Home" -> {
                                    item { Headline("Space to choose."); Text("A quieter screen. A clearer next step.", color = scheme.onSurfaceVariant) }
                                    item { ProtectionSignature(running, connected, modelAvailable, modelStatus == "Checking local model…") }
                                    item { Panel("Protection status") {
                                        Text(monitoring)
                                        Text(if (connected) "Foreground awareness connected" else "Foreground awareness off — app restrictions unavailable")
                                        Text(modelStatus, style = MaterialTheme.typography.bodySmall)
                                        Text("Capture: ${health.captureState.name.lowercase().replace('_', ' ')}", style = MaterialTheme.typography.labelMedium)
                                    } }
                                    item { Panel("Screen guardian") {
                                        Text("Experimental local screening. Protected surfaces cannot be analyzed. Phone-feed accuracy is not established.")
                                        Button(onClick = { captureDisclosure = true }, enabled = modelAvailable && connected && !running && !consentPending) { Text("Review and start screen sharing") }
                                        OutlinedButton(onClick = { MonitoringRuntime.stop(context) }) { Text("Stop screen sharing") }
                                        if (!connected) TextButton(onClick = { disclosure = true }) { Text("Set up foreground awareness") }
                                    } }
                                    item { Panel("Your commitment") {
                                        val remaining = state.focusRemaining(now, store.bootCount())
                                        Text(if (remaining > 0) {
                                            if (now < state.focusStartElapsed) "Scheduled — begins in ${timeLeft(state.focusStartElapsed - now)}" else "Focus remaining ${timeLeft(remaining)}"
                                        } else "No active focus session")
                                        TextButton(onClick = { tab = "Focus" }) { Text("Choose your focus") }
                                    } }
                                    item { Panel("Your next step") {
                                        Text(if (recoveryState.planAction.isBlank()) "Prepare a small action for a difficult moment." else "If ${recoveryState.planTrigger}, I will ${recoveryState.planAction}.")
                                        Button(onClick = { tab = "Reset" }) { Text("Take a recovery pause") }
                                    } }
                                    item { Text("${state.events.count { it.kind == "AI_INTERVENTION" }} interventions recorded. Counts reflect monitoring and exposure, not recovery success.", style = MaterialTheme.typography.bodySmall) }
                                }
                                "Focus" -> {
                                    item { Headline("Protect your attention.") }
                                    item { Text("Choose launchable apps. Veil adds a dismissible pause when they open during focus. Settings, calls and system controls stay accessible. Multi-window and OEM behavior need device verification.") }
                                    if (!connected) item { Button(onClick = { disclosure = true }) { Text("Set up foreground awareness") } }
                                    if (state.focusRemaining(now, store.bootCount()) > 0) {
                                        item { Panel("Current session") {
                                            Text("${timeLeft(state.focusRemaining(now, store.bootCount()))} remaining · ${state.focusPackages.size} apps")
                                            if (now < state.focusStartElapsed) Text("Starts in ${timeLeft(state.focusStartElapsed - now)}")
                                            OutlinedButton(onClick = { store.endFocus() }) { Text("End focus early") }
                                        } }
                                    }
                                    item {
                                        OutlinedTextField(minutes, { minutes = it.filter(Char::isDigit).take(5) }, label = { Text("Duration in minutes (1–10080)") }, singleLine = true)
                                        Spacer(Modifier.height(12.dp))
                                        OutlinedTextField(delayMinutes, { delayMinutes = it.filter(Char::isDigit).take(4) }, label = { Text("Starts in minutes (0–1440)") }, singleLine = true)
                                    }
                                    item { Button(onClick = {
                                        val duration = minutes.toIntOrNull(); val start = delayMinutes.toIntOrNull()
                                        if (duration !in 1..10080 || start !in 0..1440 || selected.isEmpty()) message = "Choose apps and valid duration / start delay."
                                        else { store.startFocus(selected.toSet(), duration!!, start!!); message = "Commitment saved. Foreground awareness must remain enabled." }
                                    }, enabled = connected) { Text("Confirm focus commitment") } }
                                    if (loadingApps) item { CircularProgressIndicator() }
                                    else if (apps.isEmpty()) item { Text("No eligible launchable apps found.") }
                                    item { OutlinedTextField(appSearch, { appSearch = it.take(80) }, label = { Text("Find an app") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                                    items(apps.filter { it.label.contains(appSearch, ignoreCase = true) }, key = { it.pkg }) { app ->
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(app.label, Modifier.weight(1f).padding(top = 12.dp))
                                            Checkbox(app.pkg in selected, onCheckedChange = { check -> selected = if (check) selected + app.pkg else selected - app.pkg }, modifier = Modifier.semantics { contentDescription = "Restrict ${app.label}" })
                                        }
                                    }
                                }
                                "Reset" -> {
                                    item { Headline("Take back control.") }
                                    item { Text(if (state.cooldownRemaining(now, store.bootCount()) > 0) "Cooldown remaining ${timeLeft(state.cooldownRemaining(now, store.bootCount()))}" else "Make a calm pause whenever you need one.") }
                                    item { Panel("Guided breathing") {
                                        Text(if (breathing) "${if (breathSeconds % 8 < 4) "Breathe in" else "Breathe out"} · ${60 - breathSeconds}s" else "One minute. Four seconds in, four seconds out. Breathe comfortably.")
                                        Button(onClick = { breathing = !breathing }) { Text(if (breathing) "Stop breathing guide" else "Begin breathing") }
                                    } }
                                    item { Panel("Reflect or move") {
                                        OutlinedTextField(reflection, { reflection = it.take(500) }, label = { Text("What do you want to return to?") }, modifier = Modifier.fillMaxWidth())
                                        Text("Reflection stays in memory until this screen is destroyed or you clear it. Alternatively, stretch, drink water or choose a few comfortable push-ups. Physical activity is optional and self reported.")
                                        Button(onClick = { reflection = ""; store.recoveryComplete(); message = "Recovery noted. Return at your own pace." }) { Text("I completed a recovery activity") }
                                    } }
                                    if (state.cooldownPackage.isNotBlank()) item {
                                        OutlinedButton(onClick = { feedback.report(com.veil.app.core.Correction.NOT_EXPLICIT); MonitoringRuntime.stop(context); store.dismissCooldown(true) }) { Text("Incorrect detection — stop guardian") }
                                        TextButton(onClick = { MonitoringRuntime.stop(context); store.dismissCooldown() }) { Text("Emergency exit — stop guardian") }
                                    }
                                    item { RecoveryExperience(recovery) }
                                    item { FeedbackExperience(feedback) }
                                    item { TextButton(onClick = { legacy = true }) { Text("Open recovery plan and local notes") } }
                                }
                                "History" -> {
                                    item { RecoveryProgress(recovery) }
                                    item { Headline("Progress, kept private."); Text("Event types and times only. Up to 100 events, retained for 30 days. No captured images or app usage history.") }
                                    if (state.events.isEmpty()) item { Text("No local events yet.") }
                                    items(state.events.reversed()) { event ->
                                        Text("${event.kind.lowercase().replace('_', ' ')}\n${DateFormat.getDateTimeInstance().format(Date(event.at))}")
                                        HorizontalDivider()
                                    }
                                    item { OutlinedButton(onClick = { store.clearHistory() }) { Text("Delete history") } }
                                }
                                "Privacy" -> {
                                    item { Headline("Private by design.") }
                                    item { Text("Veil has no internet permission. Capture and inference run in memory on this device. No frames, messages or URLs are saved. Local rules, preferences and event timestamps remain in app storage; Android backup is disabled.") }
                                    item { Panel("Permissions and control") {
                                        Text(if (connected) "Accessibility service connected." else "Accessibility service disconnected.")
                                        TextButton(onClick = { disclosure = true }) { Text("Review foreground awareness") }
                                        if (Build.VERSION.SDK_INT >= 33) TextButton(onClick = { notification.launch(Manifest.permission.POST_NOTIFICATIONS) }) { Text("Allow status notifications") }
                                        TextButton(onClick = { MonitoringRuntime.stop(context); store.endFocus(); store.dismissCooldown(); message = "Screen sharing and restrictions stopped. You can revoke accessibility in Android Settings." }) { Text("Stop all protection") }
                                    } }
                                    item { Panel("Technical diagnostics") {
                                        Text("Export service state and aggregate timing only. No screen content, app names, journal, feedback or encouragement preferences.")
                                        OutlinedButton(onClick = { export.launch("Veil-diagnostics.txt") }) { Text("Save technical diagnostics") }
                                    } }
                                    item { FeedbackExperience(feedback) }
                                    item { Panel("AI cooldown") {
                                        listOf(5, 10, 15).forEach { minutes -> FilterChip(selected = state.cooldownMinutes == minutes, onClick = { store.cooldownMinutes(minutes) }, label = { Text("$minutes minutes") }) }
                                        Text("The detector uses experimental model scores and temporal confirmation, not a diagnosis or a guarantee.")
                                    } }
                                    item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Dark appearance"); Switch(state.dark, onCheckedChange = store::theme) } }
                                    item { OutlinedButton(onClick = { MonitoringRuntime.stop(context); store.clearAll(); message = "Current rules and history deleted. Notes and legacy preferences can be erased through Android App info → Storage → Clear data." }) { Text("Delete focus rules and event history") } }
                                    item { Text("Veil · experimental engineering build\nOrdinary apps cannot prevent permission revocation, force stop or uninstall. Reboot ends commitments safely; it never restarts screen sharing. Google Play review and device qualification remain pending.") }
                                }
                            }
                        }
                    }
                }
            }
            if (disclosure) AlertDialog(onDismissRequest = { disclosure = false }, title = { Text("Foreground awareness") }, text = {
                Text("Veil uses Accessibility Service to receive the package name of the foreground app and display a dismissible protective screen for your chosen apps and AI cooldowns. It does not retrieve screen text, read messages, perform gestures or upload activity. Foreground names stay in memory; selected rules stay locally. You can turn it off in Android Settings at any time. This is voluntary wellbeing support, not an accessibility tool.", modifier = Modifier.verticalScroll(rememberScrollState()))
            }, confirmButton = { TextButton(onClick = { disclosure = false; context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }) { Text("I agree — open Settings") } }, dismissButton = { TextButton(onClick = { disclosure = false }) { Text("Not now") } })
            if (captureDisclosure) AlertDialog(onDismissRequest = { captureDisclosure = false }, title = { Text("Share your screen with Veil?") }, text = {
                Text("Android will ask to share this display. Visible frames are analyzed locally with an experimental adult-content classifier, then discarded. No images are saved or sent. Screening may be wrong or miss content; protected surfaces cannot be analyzed. A visible foreground notification and Android sharing indicator remain. Stop at any time from Android or Veil. Foreground awareness is required for app-specific interventions.", modifier = Modifier.verticalScroll(rememberScrollState()))
            }, confirmButton = { TextButton(onClick = {
                captureDisclosure = false
                consentPending = true
                val manager = context.getSystemService(MediaProjectionManager::class.java)
                val intent = if (Build.VERSION.SDK_INT >= 34) manager.createScreenCaptureIntent(MediaProjectionConfig.createConfigForDefaultDisplay()) else manager.createScreenCaptureIntent()
                capture.launch(intent)
            }) { Text("Continue to Android consent") } }, dismissButton = { TextButton(onClick = { captureDisclosure = false }) { Text("Cancel") } })
        }
    }
}

private fun timeLeft(milliseconds: Long): String = "%02d:%02d".format(milliseconds / 60_000, milliseconds / 1000 % 60)
@Composable private fun Headline(text: String) { Text(text, style = MaterialTheme.typography.headlineMedium, fontFamily = FontFamily.Serif) }
@Composable private fun Panel(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { Text(title, style = MaterialTheme.typography.titleMedium); content() } }
}

private fun navLabel(name: String): String = when (name) { "Focus" -> "Shield"; "Reset" -> "Recovery"; "History" -> "Progress"; "Privacy" -> "Settings"; else -> name }

@Composable private fun ProtectionSignature(running: Boolean, connected: Boolean, modelReady: Boolean, modelChecking: Boolean) {
    val muted = MaterialTheme.colorScheme.outlineVariant
    val ready = MaterialTheme.colorScheme.secondary
    Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(28.dp)).padding(20.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        Canvas(Modifier.size(76.dp).semantics { contentDescription = "Three independent protection indicators; details alongside" }) {
            listOf(running, connected, modelReady).forEachIndexed { i, active ->
                val inset = i * 9.dp.toPx()
                drawArc(if (active) ready else muted, -90f, 285f, false, Offset(inset, inset), Size(size.width - inset * 2, size.height - inset * 2), style = Stroke(4.dp.toPx()))
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(if (running) "Sharing session active" else "Screen sharing off", fontWeight = FontWeight.SemiBold)
            Text(if (connected) "App intervention connected" else "App intervention needs setup", style = MaterialTheme.typography.bodySmall)
            Text(if (modelReady) "Experimental model ready" else if (modelChecking) "Checking local model…" else "Model unavailable", style = MaterialTheme.typography.bodySmall)
        }
    }
}
