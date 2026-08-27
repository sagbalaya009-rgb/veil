package com.veil.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.veil.app.core.VeilStrings
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
    strings: VeilStrings,
    onRecoveryComplete: () -> Unit,
    onCooldownFinished: () -> Unit,
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
    var completionHandled by remember(state.cooldownEndsAt) { mutableStateOf(false) }
    LaunchedEffect(secondsRemaining) {
        if (secondsRemaining == 0L && !completionHandled) {
            completionHandled = true
            onCooldownFinished()
        }
    }
    val time = "%02d:%02d".format(secondsRemaining / 60, secondsRemaining % 60)
    val task = taskLabel(state.recoveryTask, strings)

    Surface(modifier = Modifier.fillMaxSize(), color = palette.background) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            item {
            Surface(color = palette.panel, shape = RoundedCornerShape(5.dp)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        strings.cooldown_started.uppercase(),
                        color = palette.primary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = androidx.compose.ui.unit.TextUnit(2f, androidx.compose.ui.unit.TextUnitType.Sp),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        strings.good_call,
                        color = palette.text,
                        style = MaterialTheme.typography.displaySmall,
                        fontFamily = FontFamily.Serif,
                    )
                    Spacer(Modifier.height(16.dp))
                    Spacer(Modifier.height(26.dp))
                    Surface(color = palette.soft, shape = RoundedCornerShape(0.dp)) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                        ) {
                            Text(strings.time_remaining.uppercase(), color = palette.softText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(6.dp))
                            Text(time, color = palette.text, style = MaterialTheme.typography.displayLarge, fontFamily = FontFamily.Serif)
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(strings.your_reset_activity.uppercase(), color = palette.softText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(7.dp))
                    Text(task, color = palette.text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    if (!recoveryNote.isNullOrBlank()) {
                        Spacer(Modifier.height(20.dp))
                        Text(strings.your_note.uppercase(), color = palette.softText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(7.dp))
                        Text("“$recoveryNote”", color = palette.text, style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif)
                    }
                    if (supportiveMessage != null) {
                        Spacer(Modifier.height(20.dp))
                        Text(supportiveMessage.reference.uppercase(), color = palette.softText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(7.dp))
                        if (supportiveMessage.arabicText != null) {
                            Text(supportiveMessage.arabicText, color = palette.text, style = MaterialTheme.typography.titleLarge, textAlign = androidx.compose.ui.text.style.TextAlign.End, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(12.dp))
                            Text(strings.transliteration.uppercase(), color = palette.softText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(5.dp))
                            Text(supportiveMessage.transliteration.orEmpty(), color = palette.muted, style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(12.dp))
                            Text(strings.translation.uppercase(), color = palette.softText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(5.dp))
                            Text("“${supportiveMessage.body}”", color = palette.text, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Serif)
                            Spacer(Modifier.height(5.dp))
                            Text(supportiveMessage.translationAttribution.orEmpty(), color = palette.muted, style = MaterialTheme.typography.bodySmall)
                        } else {
                            Text("“${supportiveMessage.body}”", color = palette.text, style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif)
                            Spacer(Modifier.height(5.dp))
                            Text(supportiveMessage.reference, color = palette.muted, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(
                        strings.preview_only,
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
                    ) { Text(strings.im_done) }
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onChangePlan,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(5.dp),
                    ) { Text(strings.change_plan, color = palette.text) }
                    Spacer(Modifier.height(4.dp))
                    androidx.compose.material3.TextButton(
                        onClick = onEmergencyOverride,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) { Text(strings.emergency_override, color = palette.muted) }
                }
            }
        }
        }
    }
}

private fun taskLabel(task: RecoveryTask, strings: VeilStrings): String = when (task) {
    RecoveryTask.PUSH_UPS -> strings.push_ups
    RecoveryTask.BREATHING -> strings.breathing
    RecoveryTask.WALK -> strings.walk
    RecoveryTask.JOURNAL -> strings.journal
    RecoveryTask.WATER -> strings.water
    RecoveryTask.STRETCH -> strings.stretch
    RecoveryTask.OFFLINE_PAUSE -> strings.offline_pause
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
