package com.veil.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.veil.app.core.Correction
import com.veil.app.core.recovery.*
import com.veil.app.services.FeedbackStore
import com.veil.app.services.RecoveryStore
import java.text.DateFormat
import java.util.Date

@Composable internal fun RecoveryExperience(store: RecoveryStore) {
    val state by store.state.collectAsStateWithLifecycle()
    var editing by rememberSaveable { mutableStateOf(false) }
    var goals by remember { mutableStateOf(state.goals) }
    var mode by remember { mutableStateOf(state.encouragement) }
    var reminder by remember { mutableStateOf(state.customReminder) }
    var planTrigger by remember { mutableStateOf(state.planTrigger) }
    var planAction by remember { mutableStateOf(state.planAction) }
    var intensity by remember { mutableStateOf<Int?>(null) }
    var trigger by remember { mutableStateOf<Trigger?>(null) }
    var note by remember { mutableStateOf("") }
    var activity by remember { mutableStateOf(RecoveryActivity.GROUNDING) }
    var lastActivityId by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf("") }
    var confirmDeletion by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (!state.storageAvailable) Text(state.storageMessage ?: "Recovery storage unavailable. Nothing new will be saved.")
        if (message.isNotBlank()) Text(message)
        state.encouragementText()?.let { Text(it, style = MaterialTheme.typography.titleMedium) }
        RecoveryPanel("Your direction") {
            Text(state.goals.joinToString { it.label }.ifBlank { "Choose a goal when you are ready. You can skip this." })
            TextButton(onClick = {
                if (!editing) { goals = state.goals; mode = state.encouragement; reminder = state.customReminder }
                editing = !editing
            }) { Text(if (editing) "Close preferences" else "Choose goals and encouragement") }
            if (editing) {
                RecoveryGoal.entries.forEach { goal ->
                    Row(Modifier.fillMaxWidth()) {
                        Checkbox(goal in goals, { checked -> goals = if (checked) goals + goal else goals - goal },
                            modifier = Modifier.semantics { contentDescription = goal.label })
                        Text(goal.label, Modifier.weight(1f).padding(top = 12.dp))
                    }
                }
                Text("Encouragement is optional. No religious identity is required.")
                EncouragementMode.entries.forEach { value ->
                    FilterChip(selected = mode == value, onClick = { mode = value }, label = { Text(value.label) })
                }
                if (mode == EncouragementMode.FAITH) {
                    OutlinedTextField(reminder, { reminder = it.take(400) }, label = { Text("Your own faith reminder (optional)") }, modifier = Modifier.fillMaxWidth())
                    Text("Use words meaningful to you. Veil supplies no religious rulings or unsourced quotations.", style = MaterialTheme.typography.bodySmall)
                }
                Button(onClick = {
                    val saved = store.configure(goals, mode, if (mode == EncouragementMode.FAITH) reminder else "")
                    message = if (saved) "Preferences saved on this device." else "Preferences could not be saved."
                    if (saved) editing = false
                }, enabled = state.storageAvailable) { Text("Save my preferences") }
                TextButton(onClick = { editing = false }) { Text("Skip for now") }
            }
        }
        RecoveryPanel("A plan for next time") {
            OutlinedTextField(planTrigger, { planTrigger = it.take(160) }, label = { Text("When I notice…") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(planAction, { planAction = it.take(160) }, label = { Text("I will try…") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = { message = if (store.savePlan(planTrigger, planAction)) "Coping plan saved." else "Plan could not be saved." }, enabled = state.storageAvailable && planTrigger.isNotBlank() && planAction.isNotBlank()) { Text("Save coping plan") }
        }
        RecoveryPanel("Choose a small reset") {
            RecoveryActivity.entries.filter { it != RecoveryActivity.BREATHING }.forEach { value ->
                FilterChip(selected = activity == value, onClick = { activity = value; lastActivityId = null }, label = { Text(value.label) })
            }
            activity.instructions.forEach { Text(it) }
            if (activity == RecoveryActivity.PERSONAL_PLAN) Text(if (state.planAction.isBlank()) "You can prepare a plan above, or choose another option." else "If ${state.planTrigger}, I will ${state.planAction}.")
            Button(onClick = {
                lastActivityId = store.completeActivity(activity)
                message = if (lastActivityId != null) "Activity noted. You choose what comes next." else "Activity could not be saved."
            }, enabled = state.storageAvailable) { Text("I tried this activity") }
            lastActivityId?.let { id ->
                Text("Was it helpful? (optional)")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = {
                        if (store.setHelpful(id, true)) { lastActivityId = null; message = "Activity feedback saved." }
                        else message = "Activity feedback could not be saved."
                    }, enabled = state.storageAvailable) { Text("Helpful") }
                    TextButton(onClick = {
                        if (store.setHelpful(id, false)) { lastActivityId = null; message = "Activity feedback saved." }
                        else message = "Activity feedback could not be saved."
                    }, enabled = state.storageAvailable) { Text("Not this time") }
                }
            }
        }
        RecoveryPanel("Optional private check-in") {
            Text("Skip any question. Only Save check-in stores these answers, encrypted on this device.")
            Text("Urge intensity: ${intensity?.toString() ?: "not recorded"}")
            Slider(value = (intensity ?: 5).toFloat(), onValueChange = { intensity = it.toInt() }, valueRange = 0f..10f, steps = 9)
            TextButton(onClick = { intensity = null }) { Text("Skip intensity") }
            Trigger.entries.forEach { value -> FilterChip(selected = trigger == value, onClick = { trigger = if (trigger == value) null else value }, label = { Text(value.label) }) }
            OutlinedTextField(note, { note = it.take(1000) }, label = { Text("Private reflection (optional)") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = {
                val id = store.logCheckIn(intensity, trigger, note)
                message = when {
                    id != null -> "Check-in saved privately."
                    !store.state.value.storageAvailable -> "Check-in could not be saved. Your answers remain here until you clear them or leave."
                    else -> "Nothing saved. Choose an answer, or skip."
                }
                if (id != null) { intensity = null; trigger = null; note = "" }
            }, enabled = state.storageAvailable) { Text("Save check-in") }
            TextButton(onClick = { intensity = null; trigger = null; note = ""; message = "Check-in skipped. Nothing saved." }) { Text("Skip check-in") }
            TextButton(onClick = {
                message = if (store.reportSetback()) "One moment does not erase your progress. Adjust your plan if it helps." else "Nothing could be saved. Your previous progress remains."
            }, enabled = state.storageAvailable) { Text("I want to note a setback") }
        }
        Text("Veil supports self-management, not diagnosis or medical treatment. If distress or disruption is substantial, consider speaking with a qualified professional.", style = MaterialTheme.typography.bodySmall)
        TextButton(onClick = { confirmDeletion = true }) { Text("Delete all V2 recovery data") }
    }
    if (confirmDeletion) AlertDialog(onDismissRequest = { confirmDeletion = false }, title = { Text("Delete recovery data?") }, text = { Text("This removes V2 goals, encouragement, coping plan and check-ins from this device. Legacy notes and protection settings are separate.") }, confirmButton = {
        TextButton(onClick = {
            val deleted = store.deleteAll()
            if (deleted) { goals = emptySet(); mode = EncouragementMode.PRACTICAL; reminder = ""; planTrigger = ""; planAction = ""; note = ""; intensity = null; trigger = null; lastActivityId = null; activity = RecoveryActivity.GROUNDING; editing = false }
            message = if (deleted) "V2 recovery data deleted." else "Deletion could not be completed."
            confirmDeletion = false
        }) { Text("Delete recovery data") }
    }, dismissButton = { TextButton(onClick = { confirmDeletion = false }) { Text("Cancel") } })
}

@Composable internal fun RecoveryProgress(store: RecoveryStore) {
    val state by store.state.collectAsStateWithLifecycle()
    RecoveryPanel("Effort, without a streak") {
        if (!state.storageAvailable) Text(state.storageMessage ?: "Recovery storage unavailable.")
        Text("${state.completedActivities} activities noted · ${state.helpfulActivities} marked helpful")
        val rated = state.checkIns.mapNotNull { it.intensity }
        Text(if (rated.isEmpty()) "No intensity ratings recorded." else "${rated.size} optional ratings · average %.1f / 10".format(rated.average()))
        Text("Only the retained 30-day window (up to 100 records). These self-reports do not prove treatment effectiveness or recovery.", style = MaterialTheme.typography.bodySmall)
        state.records.take(10).forEach { record ->
            HorizontalDivider()
            Text("${record.activity?.label ?: when(record.kind) { RecoveryRecordKind.SETBACK -> "Setback noted — progress retained"; else -> "Private check-in" }} · ${DateFormat.getDateTimeInstance().format(Date(record.atEpochMillis))}", style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = { store.deleteRecord(record.id) }) { Text("Delete this record") }
        }
        OutlinedButton(onClick = { store.deleteRecords() }) { Text("Delete recovery records") }
    }
}

@Composable internal fun FeedbackExperience(store: FeedbackStore) {
    val entries by store.state.collectAsStateWithLifecycle()
    val storageAvailable by store.storageAvailable.collectAsStateWithLifecycle()
    var message by remember { mutableStateOf("") }
    var undoUntil by remember { mutableLongStateOf(0) }
    var now by remember { mutableLongStateOf(0) }
    LaunchedEffect(undoUntil) {
        while (undoUntil > android.os.SystemClock.elapsedRealtime()) { now = android.os.SystemClock.elapsedRealtime(); kotlinx.coroutines.delay(250) }
        now = android.os.SystemClock.elapsedRealtime()
    }
    RecoveryPanel("Help assess the detector") {
        if (!storageAvailable) Text("Feedback storage is unavailable. Existing data is preserved; Reset feedback explicitly clears it.")
        Text("Was an intervention correct? Reports stay local. They never change sensitivity, train the model, exempt an app or stop sharing.")
        listOf(Correction.EXPLICIT to "Yes, this was explicit", Correction.NOT_EXPLICIT to "No, this was not explicit", Correction.UNSURE to "I'm not sure", Correction.MISSED to "Veil missed something").forEach { (kind, label) ->
            TextButton(onClick = {
                val saved = store.report(kind)
                message = if (saved) "Report saved locally. Protection is unchanged." else "Report could not be saved. Protection is unchanged."
                undoUntil = if (saved) android.os.SystemClock.elapsedRealtime() + 10_000 else 0
            }, enabled = storageAvailable) { Text(label) }
        }
        if (message.isNotBlank()) Text(message)
        if (undoUntil > now) TextButton(onClick = {
            message = if (store.undo()) "Last report undone." else if (!store.storageAvailable.value) "Report could not be undone because storage is unavailable." else "Undo window ended."
            undoUntil = 0
        }) { Text("Undo last report") }
        Text("${entries.size} local reports · up to 100 / 30 days · no images or app names", style = MaterialTheme.typography.bodySmall)
        TextButton(onClick = {
            message = if (store.clear()) "Feedback deleted. Detector policy is unchanged." else "Feedback deletion could not be completed."
            undoUntil = 0
        }) { Text("Reset feedback") }
    }
}

@Composable private fun RecoveryPanel(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}
