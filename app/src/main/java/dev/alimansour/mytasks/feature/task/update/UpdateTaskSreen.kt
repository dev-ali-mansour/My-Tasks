package dev.alimansour.mytasks.feature.task.update

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.alimansour.mytasks.R
import dev.alimansour.mytasks.core.ui.common.LaunchedUiEffectHandler
import dev.alimansour.mytasks.core.ui.utils.UiText
import dev.alimansour.mytasks.core.ui.utils.getFormattedDate
import dev.alimansour.mytasks.feature.task.TaskEffect
import dev.alimansour.mytasks.feature.task.TaskState
import dev.alimansour.mytasks.feature.task.UpdateTaskEvent
import dev.alimansour.mytasks.core.ui.theme.MyTasksTheme
import dev.alimansour.mytasks.core.ui.theme.interFamily
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateTaskScreen(
    modifier: Modifier = Modifier,
    onNavigationIconClicked: () -> Unit,
    onSuccess: (message: UiText) -> Unit,
    onSetTopBar: (@Composable () -> Unit) -> Unit,
    onSetFab: (@Composable () -> Unit) -> Unit,
    showError: (message: UiText) -> Unit,
) {
    val viewModel: UpdateTaskViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        onSetTopBar {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.update_task),
                        color = MaterialTheme.colorScheme.onSurface,
                        style =
                            MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = interFamily,
                                fontWeight = FontWeight.Bold,
                            ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigationIconClicked) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = stringResource(R.string.close),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
            )
        }
        onSetFab {}
    }

    LaunchedUiEffectHandler(
        viewModel.effect,
        onConsumeEffect = { viewModel.processEvent(UpdateTaskEvent.ConsumeEffect) },
        onEffect = { effect ->
            when (effect) {
                is TaskEffect.ShowSuccess -> onSuccess(UiText.StringResourceId(R.string.task_updated_success))
                is TaskEffect.ShowError -> {
                    showError(effect.message)
                }
            }
        },
    )

    UpdateTaskContent(modifier = modifier, uiState = uiState, onEvent = viewModel::processEvent)
}

@Composable
private fun UpdateTaskContent(
    modifier: Modifier = Modifier,
    uiState: TaskState,
    onEvent: (UpdateTaskEvent) -> Unit,
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val showDatePicker = remember { mutableStateOf(false) }

    val customTextFieldColors =
        TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
            focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
            unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
            focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
        )

    Column(
        modifier =
            modifier
                .padding(16.dp)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            TextField(
                value = uiState.title,
                onValueChange = { onEvent(UpdateTaskEvent.UpdateTitle(it)) },
                label = {
                    Text(
                        stringResource(R.string.task_title),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = customTextFieldColors,
            )

            Spacer(Modifier.height(16.dp))

            TextField(
                value = uiState.description,
                onValueChange = { onEvent(UpdateTaskEvent.UpdateDescription(it)) },
                label = {
                    Text(
                        stringResource(R.string.description),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 150.dp),
                shape = RoundedCornerShape(8.dp),
                colors = customTextFieldColors,
            )

            Spacer(Modifier.height(16.dp))

            TextField(
                value = uiState.dueDate.getFormattedDate(context),
                onValueChange = {},
                readOnly = true,
                label = {
                    Text(
                        stringResource(R.string.due_date),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                },
                singleLine = true,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                        ) {
                            showDatePicker.value = true
                        },
                colors = customTextFieldColors,
                keyboardOptions =
                    KeyboardOptions.Default.copy(
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                    ),
                trailingIcon = {
                    Icon(
                        modifier = Modifier.clickable { showDatePicker.value = true },
                        painter =
                            painterResource(
                                id = R.drawable.baseline_calendar_month_24,
                            ),
                        contentDescription = stringResource(R.string.due_date),
                    )
                },
                interactionSource = interactionSource,
                shape = RoundedCornerShape(8.dp),
            )
        }

        Button(
            onClick = { onEvent(UpdateTaskEvent.Proceed) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.Black,
                ),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(
                text = stringResource(R.string.update_task),
                modifier = Modifier.padding(8.dp),
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontFamily = interFamily,
                        fontWeight = FontWeight.Bold,
                    ),
            )
        }
    }

    if (showDatePicker.value) {
        TaskDatePicker(
            onDateSelected = { selectedDate ->
                onEvent(UpdateTaskEvent.UpdateDueDate(selectedDate))
                showDatePicker.value = false
            },
            onDismiss = {
                showDatePicker.value = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDatePicker(
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(millis)
                    }
                },
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}

@PreviewScreenSizes
@PreviewLightDark
@Composable
private fun UpdateTaskContentPreview() {
    MyTasksTheme(dynamicColor = false) {
        UpdateTaskContent(
            uiState =
                TaskState(
                    title = "Task Title",
                    description = "Task Description",
                    dueDate = System.currentTimeMillis(),
                ),
            onEvent = {},
        )
    }
}
