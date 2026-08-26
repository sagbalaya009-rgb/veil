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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.lifecycle.viewModelScope
import androidx.hilt.navigation.compose.hiltViewModel
import com.veil.app.core.CooldownDuration
import com.veil.app.core.LockPeriod
import com.veil.app.core.ProtectionEngine
import com.veil.app.core.ProtectionLevel
import com.veil.app.core.ProtectionSettings
import com.veil.app.core.RecoveryTask
import com.veil.app.data.VeilRepository
import com.veil.app.ui.InterventionScreen
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.veil.app.core.ProtectionState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val VeilBackground = Color(0xFF0B0B0F)
private val VeilSurface = Color(0xFF17171D)
private val VeilBlue = Color(0xFF385C98)
private val VeilText = Color(0xFFF7F7FA)
private val VeilMuted = Color(0xFFA7A7B0)

@AndroidEntryPoint
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
private fun VeilApp(viewModel: VeilViewModel = hiltViewModel()) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = VeilBackground,
    ) {
        if (!viewModel.onboardingComplete) {
            OnboardingScreen(onComplete = viewModel::completeOnboarding)
        } else if (viewModel.state is ProtectionState.Cooldown) {
            InterventionScreen(
                state = viewModel.state as ProtectionState.Cooldown,
                recoveryNote = viewModel.recoveryNote,
                onRecoveryComplete = viewModel::completeRecovery,
                onEmergencyOverride = viewModel::useEmergencyOverride,
            )
        } else {
            HomeScreen(
                state = viewModel.state,
                onActivate = viewModel::activate,
            )
        }
    }
}

@androidx.compose.runtime.Composable
private fun OnboardingScreen(onComplete: (ProtectionSettings) -> Unit) {
    var step by remember { mutableStateOf(0) }
    val isLastStep = step == 3
    var level by remember { mutableStateOf(ProtectionLevel.BALANCED) }
    var cooldown by remember { mutableStateOf(CooldownDuration.TEN) }
    var recoveryTask by remember { mutableStateOf(RecoveryTask.BREATHING) }
    var lockPeriod by remember { mutableStateOf(LockPeriod.TWO_HOURS) }
    var recoveryNote by remember { mutableStateOf("") }

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
                    Text("Your cooldown plan.", color = VeilText, style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Choose a small reset for when Veil later confirms content that crosses your boundary.",
                        color = VeilMuted,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(Modifier.height(28.dp))
                    Text("How long should the cooldown last?", color = VeilText, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(10.dp))
                    CooldownGrid(selected = cooldown, onSelected = { cooldown = it })
                    Spacer(Modifier.height(28.dp))
                    Text("Choose one reset.", color = VeilText, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(10.dp))
                    RecoveryGrid(selected = recoveryTask, onSelected = { recoveryTask = it })
                    Spacer(Modifier.height(24.dp))
                    OutlinedTextField(
                        value = recoveryNote,
                        onValueChange = { recoveryNote = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("A note for your cooldown") },
                        placeholder = { Text("One line is enough.") },
                        supportingText = { Text("Stored locally on this device.") },
                        maxLines = 3,
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = {
                    if (isLastStep) {
                        onComplete(
                            ProtectionSettings(
                                level = level,
                                cooldown = cooldown,
                                recoveryTask = recoveryTask,
                                lockPeriod = lockPeriod,
                                recoveryNote = recoveryNote.trim(),
                            ),
                        )
                    } else {
                        step += 1
                    }
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
private fun SetupChoice(label: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) VeilBlue else VeilSurface,
            contentColor = VeilText,
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 9.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

@androidx.compose.runtime.Composable
private fun CooldownGrid(selected: CooldownDuration, onSelected: (CooldownDuration) -> Unit) {
    CooldownDuration.entries.toList().chunked(3).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { option ->
                SetupChoice(
                    label = cooldownLabel(option),
                    selected = selected == option,
                    onClick = { onSelected(option) },
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@androidx.compose.runtime.Composable
private fun RecoveryGrid(selected: RecoveryTask, onSelected: (RecoveryTask) -> Unit) {
    val tasks = listOf(
        RecoveryTask.PUSH_UPS to ("Push-ups" to "10 gentle reps"),
        RecoveryTask.BREATHING to ("Breathing" to "2 minutes"),
        RecoveryTask.WALK to ("Walk" to "5 minutes"),
        RecoveryTask.JOURNAL to ("Journal" to "One honest line"),
        RecoveryTask.WATER to ("Water" to "Fill a glass"),
        RecoveryTask.STRETCH to ("Stretch" to "3 slow minutes"),
        RecoveryTask.OFFLINE_PAUSE to ("Offline pause" to "Put the phone down"),
    )
    tasks.chunked(2).forEach { row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            row.forEach { (task, copy) ->
                Button(
                    onClick = { onSelected(task) },
                    modifier = Modifier.width(136.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected == task) VeilBlue else VeilSurface,
                        contentColor = VeilText,
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp),
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(copy.first, style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(3.dp))
                        Text(copy.second, color = if (selected == task) VeilText else VeilMuted, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
    }
}

private fun cooldownLabel(duration: CooldownDuration): String = when (duration) {
    CooldownDuration.SIXTY -> "1 hour"
    else -> "${duration.minutes} min"
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

@HiltViewModel
class VeilViewModel @Inject constructor(
    private val repository: VeilRepository,
) : ViewModel() {
    private var settings = ProtectionSettings()
    private var engine = ProtectionEngine(settings = settings)

    var state by mutableStateOf<ProtectionState>(engine.state)
        private set
    var onboardingComplete by mutableStateOf(false)
        private set
    var interventionCount by mutableStateOf(0)
        private set
    var recoveryNote by mutableStateOf("")
        private set

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val saved = repository.loadSettings()
            settings = repository.settingsFrom(saved)
            engine = ProtectionEngine(settings = settings)
            recoveryNote = settings.recoveryNote
            onboardingComplete = saved.onboardingComplete
            interventionCount = repository.observeEventCountSnapshot()
        }
    }

    fun completeOnboarding(selectedSettings: ProtectionSettings) {
        settings = selectedSettings
        engine = ProtectionEngine(settings = settings)
        recoveryNote = settings.recoveryNote
        onboardingComplete = true
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveSettings(settings, onboardingComplete = true)
        }
    }

    fun activate() {
        state = engine.startProtection()
        recordLatestEvent()
    }

    fun completeRecovery() {
        state = engine.completeRecovery()
        recordLatestEvent()
    }

    fun useEmergencyOverride() {
        if (engine.useEmergencyOverride()) {
            state = engine.state
            recordLatestEvent()
        }
    }

    private fun recordLatestEvent() {
        val event = engine.events.lastOrNull() ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.recordEvent(event)
            interventionCount = repository.observeEventCountSnapshot()
        }
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
