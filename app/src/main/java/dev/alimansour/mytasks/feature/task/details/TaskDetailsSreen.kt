package dev.alimansour.mytasks.feature.task.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.alimansour.mytasks.R
import dev.alimansour.mytasks.core.domain.model.Task
import dev.alimansour.mytasks.core.ui.common.LaunchedUiEffectHandler
import dev.alimansour.mytasks.core.ui.utils.UiText
import dev.alimansour.mytasks.core.ui.utils.UiText.StringResourceId
import dev.alimansour.mytasks.core.ui.utils.getFormattedDate
import dev.alimansour.mytasks.core.ui.theme.MyTasksTheme
import dev.alimansour.mytasks.core.ui.theme.interFamily
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailsScreen(
    modifier: Modifier = Modifier,
    onNavigationIconClicked: () -> Unit,
    onUpdateTaskClicked: (Task) -> Unit,
    onSuccess: (message: UiText) -> Unit,
    onSetTopBar: (@Composable () -> Unit) -> Unit,
    onSetFab: (@Composable () -> Unit) -> Unit,
    showError: (message: UiText) -> Unit,
) {
    val viewModel: TaskDetailsViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        onSetTopBar {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.task_details),
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
        onConsumeEffect = { viewModel.processEvent(TaskDetailsEvent.ConsumeEffect) },
        onEffect = { effect ->
            when (effect) {
                is TaskDetailsEffect.NavigateToUpdateScreen -> onUpdateTaskClicked(effect.task)
                is TaskDetailsEffect.ShowSuccess -> onSuccess(StringResourceId(R.string.task_updated_success))
                is TaskDetailsEffect.ShowError -> {
                    showError(effect.message)
                }
            }
        },
    )

    TaskDetailsContent(modifier = modifier, uiState = uiState, onEvent = viewModel::processEvent)
}

@Composable
private fun TaskDetailsContent(
    modifier: Modifier = Modifier,
    uiState: TaskDetailsState,
    onEvent: (TaskDetailsEvent) -> Unit,
) {
    val context = LocalContext.current
    uiState.task?.let { task ->

        Column(
            modifier =
                modifier
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyLarge,
//                    color = onBackgroundColor,
                    lineHeight = 24.sp,
                )

                Spacer(Modifier.height(24.dp))

                DetailItem(label = stringResource(R.string.due_date), value = task.dueDate.getFormattedDate(context = context, pattern = "MMMM dd, yyyy"))

                Spacer(Modifier.height(16.dp))

                DetailItem(
                    label = stringResource(R.string.status),
                    value =
                        if (task.isCompleted) {
                            stringResource(R.string.completed)
                        } else {
                            stringResource(R.string.in_progress)
                        },
                )
            }

            Column {
                Button(
                    onClick = { onEvent(TaskDetailsEvent.UpdateTask) },
                    modifier =
                        Modifier
                            .fillMaxWidth(),
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

                Spacer(Modifier.height(8.dp))

                FilledTonalButton(
                    onClick = { onEvent(TaskDetailsEvent.DeleteTask) },
                    modifier =
                        Modifier
                            .fillMaxWidth(),
                    colors =
                        ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.delete_task),
                        modifier = Modifier.padding(8.dp),
                        style =
                            MaterialTheme.typography.titleMedium.copy(
                                fontFamily = interFamily,
                                fontWeight = FontWeight.Bold,
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailItem(
    label: String,
    value: String,
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, // Grayish text for label
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground, // Main text color
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@PreviewScreenSizes
@PreviewLightDark
@Composable
private fun TaskDetailsContentPreview() {
    MyTasksTheme(dynamicColor = false) {
        TaskDetailsContent(
            uiState =
                TaskDetailsState(
                    task =
                        Task(
                            title = "Task Title",
                            description = "Task Description",
                            dueDate = System.currentTimeMillis(),
                            isCompleted = false,
                        ),
                ),
            onEvent = {},
        )
    }
}
