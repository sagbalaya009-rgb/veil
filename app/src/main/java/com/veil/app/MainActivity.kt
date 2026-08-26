package com.veil.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.veil.app.core.ProtectionEngine
import com.veil.app.core.ProtectionState
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val VeilBackground = Color(0xFF0B0B0F)
private val VeilSurface = Color(0xFF17171D)
private val VeilBlue = Color(0xFF385C98)
private val VeilText = Color(0xFFF7F7FA)
private val VeilMuted = Color(0xFFA7A7B0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VeilTheme {
                VeilApp()
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun VeilApp(viewModel: VeilViewModel = viewModel()) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = VeilBackground,
    ) {
        if (viewModel.onboardingComplete) {
            HomeScreen(
                state = viewModel.state,
                onActivate = viewModel::activate,
            )
        } else {
            OnboardingScreen(onComplete = viewModel::completeOnboarding)
        }
    }
}

@androidx.compose.runtime.Composable
private fun OnboardingScreen(onComplete: () -> Unit) {
    var step by remember { mutableStateOf(0) }
    val isLastStep = step == 3

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = "VEIL",
                color = VeilMuted,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(48.dp))
            when (step) {
                0 -> {
                    Text(
                        text = "Protect the commitments\nyou make to yourself.",
                        color = VeilText,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "A calm, user-controlled pause when you need it.",
                        color = VeilMuted,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                1 -> {
                    Text("Privacy first.", color = VeilText, style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Your screen is analyzed locally. Nothing is uploaded. Nothing is stored.",
                        color = VeilMuted,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                2 -> {
                    Text("Permissions.", color = VeilText, style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Veil will explain each permission before asking. You remain in control, and protection becomes reduced if a permission is revoked.",
                        color = VeilMuted,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                else -> {
                    Text("Protection setup.", color = VeilText, style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Choose your protection level, cooldown, recovery task, and lock period next.",
                        color = VeilMuted,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = {
                    if (isLastStep) onComplete() else step += 1
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VeilBlue,
                    contentColor = VeilText,
                ),
            ) {
                Text(
                    text = when {
                        step == 0 -> "Continue"
                        step == 1 -> "I understand"
                        step == 2 -> "Continue"
                        else -> "Continue to Veil"
                    },
                    modifier = Modifier.padding(vertical = 6.dp),
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "${step + 1} of 4",
                color = VeilMuted,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@androidx.compose.runtime.Composable
private fun HomeScreen(
    state: ProtectionState,
    onActivate: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = "VEIL",
                color = VeilMuted,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(32.dp))
            Text(
                text = "Protect the commitments\nyou make to yourself.",
                color = VeilText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(28.dp))
            StatusCard(state)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (state is ProtectionState.Inactive) {
                Button(
                    onClick = onActivate,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VeilBlue,
                        contentColor = VeilText,
                    ),
                ) {
                    Text("Activate protection", modifier = Modifier.padding(vertical = 6.dp))
                }
            } else {
                TextButton(onClick = {}, enabled = false) {
                    Text("Protection is active", color = VeilMuted)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Local processing. Nothing uploaded.",
                color = VeilMuted,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@androidx.compose.runtime.Composable
private fun StatusCard(state: ProtectionState) {
    val (title, detail) = when (state) {
        ProtectionState.Inactive -> "Protection" to "Not active"
        is ProtectionState.Active -> "Protection" to "Active · ends ${formatTime(state.lockEndsAt)}"
        is ProtectionState.Cooldown -> "Cooldown" to "Recovery required · ends ${formatTime(state.cooldownEndsAt)}"
        is ProtectionState.Degraded -> "Protection" to "Degraded · ${state.reason}"
        ProtectionState.PermissionRequired -> "Protection" to "Permission required"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = VeilSurface,
        shape = RoundedCornerShape(24.dp),
    ) {
        Row(
            modifier = Modifier.padding(22.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(title, color = VeilMuted, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Text(detail, color = VeilText, style = MaterialTheme.typography.titleLarge)
            }
            Text("◉", color = VeilBlue, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

private fun formatTime(instant: Instant): String = DateTimeFormatter
    .ofPattern("h:mm a")
    .withZone(ZoneId.systemDefault())
    .format(instant)

class VeilViewModel : ViewModel() {
    private val engine = ProtectionEngine()
    var state by mutableStateOf<ProtectionState>(engine.state)
        private set
    var onboardingComplete by mutableStateOf(false)
        private set

    fun completeOnboarding() {
        onboardingComplete = true
    }

    fun activate() {
        state = engine.startProtection()
    }
}

@androidx.compose.runtime.Composable
private fun VeilTheme(content: @androidx.compose.runtime.Composable () -> Unit) {
    MaterialTheme(
        colorScheme = androidx.compose.material3.darkColorScheme(
            background = VeilBackground,
            surface = VeilSurface,
            primary = VeilBlue,
            onBackground = VeilText,
            onSurface = VeilText,
        ),
        content = content,
    )
}
