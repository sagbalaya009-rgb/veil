package com.veil.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.veil.app.core.CooldownDuration
import com.veil.app.core.LockPeriod
import com.veil.app.core.ProtectedApplication
import com.veil.app.core.ProtectionEngine
import com.veil.app.core.ProtectionSettings
import com.veil.app.core.ProtectionState
import com.veil.app.core.RecoveryTask
import com.veil.app.data.VeilRepository
import com.veil.app.ui.InterventionScreen
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Website-fidelity Android shell: calm paper surfaces, blue rules, and one focused choice at a time. */
private data class VeilPalette(
    val background: Color,
    val text: Color,
    val muted: Color,
    val line: Color,
    val panel: Color,
    val soft: Color,
    val softText: Color,
    val primary: Color,
)

private val LightPalette = VeilPalette(
    background = Color(0xFFF7F6F3),
    text = Color(0xFF1C1D20),
    muted = Color(0xFF6B6A68),
    line = Color(0xFFDEDDB8),
    panel = Color.White,
    soft = Color(0xFFE9EDF5),
    softText = Color(0xFF385C98),
    primary = Color(0xFF385C98),
)

private val DarkPalette = VeilPalette(
    background = Color(0xFF17181C),
    text = Color(0xFFF1EEE8),
    muted = Color(0xFFB0AEA9),
    line = Color(0xFF393A40),
    panel = Color(0xFF202126),
    soft = Color(0xFF26334A),
    softText = Color(0xFFB7C9EA),
    primary = Color(0xFF385C98),
)

private enum class VeilDestination { HOME, RESET, NOTE }

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { VeilApp() }
    }
}

@Composable
private fun VeilApp(viewModel: VeilViewModel = hiltViewModel()) {
    var destinationName by remember { mutableStateOf(VeilDestination.HOME.name) }
    val destination = VeilDestination.valueOf(destinationName)
    var darkMode by remember { mutableStateOf(false) }
    var selectedCooldown by remember { mutableStateOf(CooldownDuration.TEN) }
    var selectedTask by remember { mutableStateOf(RecoveryTask.PUSH_UPS) }
    var draftNote by remember { mutableStateOf("") }
    var noteSaved by remember { mutableStateOf(false) }
    val palette = if (darkMode) DarkPalette else LightPalette

    LaunchedEffect(viewModel.settings) {
        selectedCooldown = viewModel.settings.cooldown
        selectedTask = viewModel.settings.recoveryTask
    }
    LaunchedEffect(viewModel.recoveryNote) {
        draftNote = viewModel.recoveryNote
        noteSaved = viewModel.recoveryNote.isNotBlank()
    }

    VeilTheme(palette) {
        Surface(modifier = Modifier.fillMaxSize(), color = palette.background) {
            val cooldownState = viewModel.state as? ProtectionState.Cooldown
            if (cooldownState != null) {
                InterventionScreen(
                    state = cooldownState,
                    recoveryNote = viewModel.recoveryNote,
                    darkMode = darkMode,
                    onRecoveryComplete = viewModel::completeRecovery,
                    onChangePlan = {
                        viewModel.cancelPreviewCooldown()
                        destinationName = VeilDestination.RESET.name
                    },
                    onEmergencyOverride = viewModel::useEmergencyOverride,
                )
            } else {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                ) {
                    val contentHeight = maxHeight - 86.dp
                    Column(modifier = Modifier.fillMaxSize()) {
                        VeilHeader(
                            destination = destination,
                            darkMode = darkMode,
                            palette = palette,
                            onDestination = { destinationName = it.name },
                            onToggleTheme = { darkMode = !darkMode },
                        )
                        Box(modifier = Modifier.fillMaxWidth().height(contentHeight)) {
                            when (destination) {
                                VeilDestination.HOME -> HomePanel(
                                    palette = palette,
                                    onSetCooldown = { destinationName = VeilDestination.RESET.name },
                                    onWriteNote = { destinationName = VeilDestination.NOTE.name },
                                )
                                VeilDestination.RESET -> ResetPanel(
                                    palette = palette,
                                    selectedCooldown = selectedCooldown,
                                    selectedTask = selectedTask,
                                    onCooldownSelected = { selectedCooldown = it },
                                    onTaskSelected = { selectedTask = it },
                                    onBack = { destinationName = VeilDestination.HOME.name },
                                    onStart = { viewModel.startPreviewCooldown(selectedCooldown, selectedTask) },
                                )
                                VeilDestination.NOTE -> NotePanel(
                                    palette = palette,
                                    note = draftNote,
                                    saved = noteSaved,
                                    onNoteChange = {
                                        draftNote = it
                                        noteSaved = false
                                    },
                                    onBack = { destinationName = VeilDestination.HOME.name },
                                    onSave = {
                                        viewModel.saveRecoveryNote(draftNote.trim())
                                        noteSaved = draftNote.trim().isNotBlank()
                                    },
                                )
                            }
                        }
                        VeilFooter(palette)
                    }
                }
            }
        }
    }
}

@Composable
private fun VeilHeader(
    destination: VeilDestination,
    darkMode: Boolean,
    palette: VeilPalette,
    onDestination: (VeilDestination) -> Unit,
    onToggleTheme: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(47.dp)
            .padding(bottom = 12.dp),
    ) {
        Row(modifier = Modifier.align(Alignment.CenterStart), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = palette.primary, shape = RoundedCornerShape(6.dp)) {
                Box(
                    modifier = Modifier
                        .width(35.dp)
                        .height(35.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("◌", color = Color.White, style = MaterialTheme.typography.titleLarge)
                }
            }
            Spacer(Modifier.width(10.dp))
            Text(
                "VEIL",
                color = palette.text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = androidx.compose.ui.unit.TextUnit(3f, androidx.compose.ui.unit.TextUnitType.Sp),
            )
        }
        Row(modifier = Modifier.align(Alignment.CenterEnd), verticalAlignment = Alignment.CenterVertically) {
            VeilNavigationButton("Home", destination == VeilDestination.HOME, palette) { onDestination(VeilDestination.HOME) }
            VeilNavigationButton("Reset", destination == VeilDestination.RESET, palette) { onDestination(VeilDestination.RESET) }
            VeilNavigationButton("Note", destination == VeilDestination.NOTE, palette) { onDestination(VeilDestination.NOTE) }
            TextButton(
                onClick = onToggleTheme,
                modifier = Modifier.background(palette.soft, RoundedCornerShape(6.dp)),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 7.dp),
            ) {
                Text(if (darkMode) "☀" else "◐", color = palette.softText)
            }
        }
    }
    HorizontalDivider(color = palette.line)
}

@Composable
private fun VeilNavigationButton(label: String, selected: Boolean, palette: VeilPalette, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.background(if (selected) palette.soft else Color.Transparent, RoundedCornerShape(6.dp)),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 7.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = if (selected) palette.softText else palette.muted),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun HomePanel(palette: VeilPalette, onSetCooldown: () -> Unit, onWriteNote: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Eyebrow("Private reset space", palette)
        Spacer(Modifier.height(14.dp))
        Text(
            "What would help\nright now?",
            color = palette.text,
            style = MaterialTheme.typography.displaySmall,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Normal,
        )
        Spacer(Modifier.height(18.dp))
        Text(
            "Choose a small reset. Veil does not watch your device or send your activity anywhere.",
            color = palette.muted,
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(42.dp))
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val primaryWidth = maxWidth * 0.57f
            val secondaryWidth = maxWidth - primaryWidth - 12.dp
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HomeActionCard(
                    modifier = Modifier.width(primaryWidth),
                    marker = "◴",
                    title = "Set your cooldown",
                    detail = "Pick time and one helpful action.",
                    primary = true,
                    palette = palette,
                    onClick = onSetCooldown,
                )
                HomeActionCard(
                    modifier = Modifier.width(secondaryWidth),
                    marker = "✎",
                    title = "Write a note",
                    detail = "Leave one line for yourself.",
                    primary = false,
                    palette = palette,
                    onClick = onWriteNote,
                )
            }
        }
    }
}

@Composable
private fun HomeActionCard(
    modifier: Modifier,
    marker: String,
    title: String,
    detail: String,
    primary: Boolean,
    palette: VeilPalette,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(142.dp),
        shape = RoundedCornerShape(5.dp),
        colors = ButtonDefaults.buttonColors(containerColor = palette.panel, contentColor = palette.text),
        contentPadding = PaddingValues(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(marker, color = if (primary) palette.primary else palette.muted, style = MaterialTheme.typography.titleLarge)
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(5.dp))
                Text(detail, color = palette.muted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ResetPanel(
    palette: VeilPalette,
    selectedCooldown: CooldownDuration,
    selectedTask: RecoveryTask,
    onCooldownSelected: (CooldownDuration) -> Unit,
    onTaskSelected: (RecoveryTask) -> Unit,
    onBack: () -> Unit,
    onStart: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 30.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
                Text("‹  Back", color = palette.muted)
            }
        }
        item {
            Eyebrow("Cooldown plan", palette)
            Spacer(Modifier.height(14.dp))
            Text("Decide what\nwill help.", color = palette.text, style = MaterialTheme.typography.displaySmall, fontFamily = FontFamily.Serif)
        }
        item {
            SettingSection(title = "How long do you want this reset to last?", palette = palette) {
                DurationChoices(selectedCooldown, palette, onCooldownSelected)
            }
        }
        item {
            SettingSection(title = "Choose one reset.", palette = palette) {
                RecoveryChoices(selectedTask, palette, onTaskSelected)
            }
        }
        item {
            Surface(color = palette.panel, shape = RoundedCornerShape(5.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.width(190.dp)) {
                        Text("Quiet completion notification", color = palette.text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(3.dp))
                        Text("Available when native notifications are added.", color = palette.muted, style = MaterialTheme.typography.bodySmall)
                    }
                    OutlinedButton(onClick = {}, enabled = false, shape = RoundedCornerShape(5.dp)) { Text("Turn on") }
                }
            }
        }
        item {
            Text(
                "Android preview only: this starts a local cooldown. Veil does not yet inspect other apps or blur your phone.",
                color = palette.muted,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        item {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                Text("${durationLabel(selectedCooldown)} · ${taskLabel(selectedTask)}", color = palette.muted, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = onStart,
                    shape = RoundedCornerShape(5.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = palette.primary, contentColor = Color.White),
                ) { Text("Test cooldown") }
            }
        }
    }
}

@Composable
private fun SettingSection(title: String, palette: VeilPalette, content: @Composable () -> Unit) {
    Column {
        Text(title, color = palette.text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(14.dp))
        content()
    }
}

@Composable
private fun DurationChoices(selected: CooldownDuration, palette: VeilPalette, onSelected: (CooldownDuration) -> Unit) {
    val durations = listOf(CooldownDuration.TEN, CooldownDuration.FIFTEEN, CooldownDuration.THIRTY, CooldownDuration.FORTY_FIVE, CooldownDuration.SIXTY)
    durations.chunked(3).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { option ->
                val isSelected = selected == option
                OutlinedButton(
                    onClick = { onSelected(option) },
                    shape = RoundedCornerShape(5.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) palette.primary else palette.line),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSelected) palette.soft else palette.panel,
                        contentColor = if (isSelected) palette.softText else palette.muted,
                    ),
                ) { Text(durationLabel(option)) }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun RecoveryChoices(selected: RecoveryTask, palette: VeilPalette, onSelected: (RecoveryTask) -> Unit) {
    val tasks = listOf(
        RecoveryTask.PUSH_UPS to ("Push-ups" to "10 gentle reps"),
        RecoveryTask.BREATHING to ("Breathing" to "2 minutes"),
        RecoveryTask.WALK to ("Walk" to "5 minutes"),
        RecoveryTask.JOURNAL to ("Journal" to "One honest line"),
        RecoveryTask.WATER to ("Water" to "Fill a glass"),
        RecoveryTask.STRETCH to ("Stretch" to "3 slow minutes"),
        RecoveryTask.OFFLINE_PAUSE to ("Offline pause" to "Put the phone down"),
    )
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val cardWidth = (maxWidth - 8.dp) / 2
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tasks.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { (task, copy) ->
                        val isSelected = selected == task
                        OutlinedButton(
                            onClick = { onSelected(task) },
                            modifier = Modifier.width(cardWidth).height(68.dp),
                            shape = RoundedCornerShape(5.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) palette.primary else palette.line),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) palette.soft else palette.panel,
                                contentColor = palette.text,
                            ),
                            contentPadding = PaddingValues(10.dp),
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(copy.first, textAlign = TextAlign.Start, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(3.dp))
                                Text(copy.second, color = if (isSelected) palette.softText else palette.muted, textAlign = TextAlign.Start, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotePanel(
    palette: VeilPalette,
    note: String,
    saved: Boolean,
    onNoteChange: (String) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 30.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) { Text("‹  Back", color = palette.muted) }
            Spacer(Modifier.height(30.dp))
            Eyebrow("A note for yourself", palette)
            Spacer(Modifier.height(14.dp))
            Text("What do you want\nto remember?", color = palette.text, style = MaterialTheme.typography.displaySmall, fontFamily = FontFamily.Serif)
            Spacer(Modifier.height(28.dp))
            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                placeholder = { Text("One line is enough.", color = palette.muted) },
                shape = RoundedCornerShape(5.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = palette.primary,
                    unfocusedBorderColor = palette.line,
                    focusedTextColor = palette.text,
                    unfocusedTextColor = palette.text,
                    focusedContainerColor = palette.panel,
                    unfocusedContainerColor = palette.panel,
                ),
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("Stored locally on this device.", color = palette.muted, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onSave,
                enabled = note.trim().isNotEmpty(),
                shape = RoundedCornerShape(5.dp),
                colors = ButtonDefaults.buttonColors(containerColor = palette.primary, contentColor = Color.White),
            ) { Text(if (saved) "Saved" else "Save note") }
        }
    }
}

@Composable
private fun Eyebrow(text: String, palette: VeilPalette) {
    Text(
        text.uppercase(),
        color = palette.primary,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = androidx.compose.ui.unit.TextUnit(2f, androidx.compose.ui.unit.TextUnitType.Sp),
    )
}

@Composable
private fun VeilFooter(palette: VeilPalette) {
    Column {
        HorizontalDivider(color = palette.line)
        Spacer(Modifier.height(12.dp))
        Text("No tracking · no screen capture · no device monitoring", color = palette.muted, style = MaterialTheme.typography.labelSmall)
    }
}

private fun durationLabel(duration: CooldownDuration): String = when (duration) {
    CooldownDuration.SIXTY -> "1 hour"
    else -> "${duration.minutes} min"
}

private fun taskLabel(task: RecoveryTask): String = when (task) {
    RecoveryTask.PUSH_UPS -> "Push-ups"
    RecoveryTask.BREATHING -> "Breathing"
    RecoveryTask.WALK -> "Walk"
    RecoveryTask.JOURNAL -> "Journal"
    RecoveryTask.WATER -> "Water"
    RecoveryTask.STRETCH -> "Stretch"
    RecoveryTask.OFFLINE_PAUSE -> "Offline pause"
}

@HiltViewModel
class VeilViewModel @Inject constructor(
    private val repository: VeilRepository,
) : ViewModel() {
    var settings by mutableStateOf(ProtectionSettings())
        private set
    private var engine = ProtectionEngine(settings = settings)

    var state by mutableStateOf<ProtectionState>(engine.state)
        private set
    var recoveryNote by mutableStateOf("")
        private set

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val saved = repository.loadSettings()
            settings = repository.settingsFrom(saved)
            engine = ProtectionEngine(settings = settings)
            state = engine.state
            recoveryNote = settings.recoveryNote
        }
    }

    fun saveRecoveryNote(note: String) {
        settings = settings.copy(recoveryNote = note)
        recoveryNote = note
        persistSettings()
    }

    /** Local UI preview only: it does not capture, inspect, or retain any screen content. */
    fun startPreviewCooldown(cooldown: CooldownDuration, recoveryTask: RecoveryTask) {
        settings = settings.copy(cooldown = cooldown, recoveryTask = recoveryTask)
        engine = ProtectionEngine(settings = settings)
        engine.startProtection()
        state = engine.registerDetection(
            confidence = ProtectionSettings.INITIAL_THRESHOLD,
            triggeringApplication = ProtectedApplication("com.veil.app"),
        )
        persistSettings()
    }

    fun cancelPreviewCooldown() {
        engine = ProtectionEngine(settings = settings)
        state = engine.state
    }

    fun completeRecovery() {
        state = engine.completeRecovery()
    }

    fun useEmergencyOverride() {
        if (engine.useEmergencyOverride()) state = engine.state
    }

    private fun persistSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveSettings(settings, onboardingComplete = true)
        }
    }
}

@Composable
private fun VeilTheme(palette: VeilPalette, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = androidx.compose.material3.lightColorScheme(
            primary = palette.primary,
            background = palette.background,
            surface = palette.panel,
            onPrimary = Color.White,
            onBackground = palette.text,
            onSurface = palette.text,
        ),
        content = content,
    )
}
