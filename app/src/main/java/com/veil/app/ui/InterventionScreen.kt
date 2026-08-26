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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.veil.app.core.ProtectionState
import com.veil.app.core.RecoveryTask
import java.time.Duration
import java.time.Instant

private val InterventionBlack = Color.Black
private val InterventionText = Color(0xFFF7F7FA)
private val InterventionMuted = Color(0xFFA7A7B0)
private val InterventionBlue = Color(0xFF385C98)

@Composable
fun InterventionScreen(
    state: ProtectionState.Cooldown,
    now: Instant = Instant.now(),
    onRecoveryComplete: () -> Unit,
    onEmergencyOverride: () -> Unit,
) {
    val secondsRemaining = Duration.between(now, state.cooldownEndsAt).seconds.coerceAtLeast(0)
    val minutes = secondsRemaining / 60
    val seconds = secondsRemaining % 60
    val task = when (state.recoveryTask) {
        RecoveryTask.PUSH_UPS -> "Complete your push-ups."
        RecoveryTask.BREATHING -> "Take two minutes to breathe."
        RecoveryTask.WALK -> "Take a short walk."
        RecoveryTask.JOURNAL -> "Write one honest sentence."
    }

    Surface(modifier = Modifier.fillMaxSize(), color = InterventionBlack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("◉", color = InterventionBlue, style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(28.dp))
            Text(
                text = "Take back control.",
                color = InterventionText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Explicit content was detected. You asked Veil to protect this commitment.",
                color = InterventionMuted,
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(36.dp))
            Text(
                text = "%02d:%02d".format(minutes, seconds),
                color = InterventionText,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Light,
            )
            Spacer(Modifier.height(28.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF17171D),
                shape = RoundedCornerShape(20.dp),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Recovery task", color = InterventionMuted, style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(task, color = InterventionText, style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onRecoveryComplete,
                enabled = secondsRemaining == 0L,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = InterventionBlue,
                    contentColor = InterventionText,
                ),
            ) {
                Text("I completed this", modifier = Modifier.padding(vertical = 6.dp))
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onEmergencyOverride,
                enabled = secondsRemaining > 0L,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(contentColor = InterventionMuted),
            ) {
                Text("Emergency override")
            }
        }
    }
}
