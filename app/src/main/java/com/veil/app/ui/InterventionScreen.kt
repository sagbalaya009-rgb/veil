package com.veil.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.veil.app.core.ProtectionState
import com.veil.app.core.RecoveryTask
import com.veil.app.core.SupportiveMessage
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.delay

/** Website-fidelity cooldown panel; it presents local app state and never captures screen content. */
@Composable
fun InterventionScreen(
    state: ProtectionState.Cooldown,
    now: Instant = Instant.now(),
    recoveryNote: String? = null,
    supportiveMessage: SupportiveMessage? = null,
    darkMode: Boolean,
    onRecoveryComplete: () -> Unit,
    onChangePlan: () -> Unit,
    onEmergencyOverride: () -> Unit,
) {
    val palette = if (darkMode) CooldownPalette.Dark else CooldownPalette.Light
    var currentTime by remember(state.cooldownEndsAt) { mutableStateOf(now) }
    LaunchedEffect(state.cooldownEndsAt) {
        while (currentTime < state.cooldownEndsAt) {
            delay(1_000)
            currentTime = Instant.now()
        }
    }
    val secondsRemaining = Duration.between(currentTime, state.cooldownEndsAt).seconds.coerceAtLeast(0)
    val time = "%02d:%02d".format(secondsRemaining / 60, secondsRemaining % 60)
    val task = taskCopy(state.recoveryTask)

    Surface(modifier = Modifier.fillMaxSize(), color = palette.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(color = palette.panel, shape = RoundedCornerShape(5.dp)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "COOLDOWN STARTED",
                        color = palette.primary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = androidx.compose.ui.unit.TextUnit(2f, androidx.compose.ui.unit.TextUnitType.Sp),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Good call. You chose a pause.",
                        color = palette.text,
                        style = MaterialTheme.typography.displaySmall,
                        fontFamily = FontFamily.Serif,
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Your local ${state.recoveryTask.name.lowercase().replace('_', ' ')} reset is active. You made a thoughtful choice for yourself.",
                        color = palette.muted,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(26.dp))
                    Surface(color = palette.soft, shape = RoundedCornerShape(0.dp)) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                        ) {
                            Text("TIME REMAINING", color = palette.softText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(6.dp))
                            Text(time, color = palette.text, style = MaterialTheme.typography.displayLarge, fontFamily = FontFamily.Serif)
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Text("YOUR RESET ACTIVITY", color = palette.softText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(7.dp))
                    Text(task.first, color = palette.text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(3.dp))
                    Text(task.second, color = palette.muted, style = MaterialTheme.typography.bodyMedium)
                    if (!recoveryNote.isNullOrBlank()) {
                        Spacer(Modifier.height(20.dp))
                        Text("YOUR NOTE", color = palette.softText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(7.dp))
                        Text("“$recoveryNote”", color = palette.text, style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif)
                    }
                    if (supportiveMessage != null) {
                        Spacer(Modifier.height(20.dp))
                        Text(supportiveMessage.label.uppercase(), color = palette.softText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(7.dp))
                        Text("“${supportiveMessage.body}”", color = palette.text, style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif)
                        Spacer(Modifier.height(5.dp))
                        Text(supportiveMessage.reference, color = palette.muted, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "Android preview only: this screen is local to Veil. It does not yet inspect other apps or blur your phone.",
                        color = palette.muted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = onRecoveryComplete,
                        enabled = secondsRemaining == 0L,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(5.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = palette.primary, contentColor = Color.White),
                    ) { Text("I’m done") }
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onChangePlan,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(5.dp),
                    ) { Text("Change plan", color = palette.text) }
                    Spacer(Modifier.height(4.dp))
                    androidx.compose.material3.TextButton(
                        onClick = onEmergencyOverride,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) { Text("Emergency override", color = palette.muted) }
                }
            }
        }
    }
}

private fun taskCopy(task: RecoveryTask): Pair<String, String> = when (task) {
    RecoveryTask.PUSH_UPS -> "Push-ups" to "10 gentle reps"
    RecoveryTask.BREATHING -> "Breathing" to "2 minutes"
    RecoveryTask.WALK -> "Walk" to "5 minutes"
    RecoveryTask.JOURNAL -> "Journal" to "One honest line"
    RecoveryTask.WATER -> "Water" to "Fill a glass"
    RecoveryTask.STRETCH -> "Stretch" to "3 slow minutes"
    RecoveryTask.OFFLINE_PAUSE -> "Offline pause" to "Put the phone down"
}

private data class CooldownPalette(
    val background: Color,
    val text: Color,
    val muted: Color,
    val panel: Color,
    val soft: Color,
    val softText: Color,
    val primary: Color,
) {
    companion object {
        val Light = CooldownPalette(
            background = Color(0xFFF7F6F3),
            text = Color(0xFF1C1D20),
            muted = Color(0xFF6B6A68),
            panel = Color.White,
            soft = Color(0xFFE9EDF5),
            softText = Color(0xFF385C98),
            primary = Color(0xFF385C98),
        )
        val Dark = CooldownPalette(
            background = Color(0xFF17181C),
            text = Color(0xFFF1EEE8),
            muted = Color(0xFFB0AEA9),
            panel = Color(0xFF202126),
            soft = Color(0xFF26334A),
            softText = Color(0xFFB7C9EA),
            primary = Color(0xFF385C98),
        )
    }
}
