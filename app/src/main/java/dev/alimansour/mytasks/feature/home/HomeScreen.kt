package dev.alimansour.mytasks.feature.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.alimansour.mytasks.R
import dev.alimansour.mytasks.core.domain.model.Task
import dev.alimansour.mytasks.core.ui.common.LaunchedUiEffectHandler
import dev.alimansour.mytasks.core.ui.navigation.Route
import dev.alimansour.mytasks.core.ui.utils.UiText
import dev.alimansour.mytasks.ui.theme.MyTasksTheme
import dev.alimansour.mytasks.ui.theme.interFamily
import org.koin.androidx.compose.koinViewModel
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateToRoute: (Route) -> Unit,
    navigateToTaskDetails: (Task) -> Unit,
    onSetTopBar: (@Composable () -> Unit) -> Unit,
    onSetFab: (@Composable () -> Unit) -> Unit,
    onFabClick: () -> Unit,
    showError: (message: UiText) -> Unit,
) {
    val viewModel: HomeViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        onSetTopBar {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        color = MaterialTheme.colorScheme.onSurface,
                        style =
                            MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = interFamily,
                                fontWeight = FontWeight.Bold,
                            ),
                    )
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
    }

    LaunchedEffect(uiState.isFabExpanded) {
        onSetFab {
            ExtendedFloatingActionButton(
                onClick = { onFabClick() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                expanded = uiState.isFabExpanded,
                text = { Text(text = "Add task") },
                icon = {
                    Icon(Icons.Filled.Add, contentDescription = "Add task")
                },
            )
        }
    }

    LaunchedUiEffectHandler(
        viewModel.effect,
        onConsumeEffect = { viewModel.processEvent(HomeEvent.ConsumeEffect) },
        onEffect = { effect ->
            when (effect) {
                is HomeEffect.NavigateToRoute -> navigateToRoute(effect.route)
                is HomeEffect.NavigateToTaskDetails -> navigateToTaskDetails(effect.task)
                is HomeEffect.ShowError -> {
                    showError(effect.message)
                }
            }
        },
    )

    HomeContent(uiState = uiState, onEvent = viewModel::processEvent)
}

@Composable
private fun HomeContent(
    uiState: HomeState,
    onEvent: (HomeEvent) -> Unit,
) {
    val listState = rememberLazyListState()
    val expandedFabState =
        remember {
            derivedStateOf {
                listState.firstVisibleItemIndex == 0
            }
        }
    LaunchedEffect(key1 = expandedFabState.value) {
        onEvent(HomeEvent.OnExpandStateChanged(expandedFabState.value))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
    ) {
        items(uiState.tasks) { task ->
            TaskItem(
                task = task,
                onCheckedChange = {
                    onEvent(HomeEvent.OnTaskCheckChanged(task.copy(isCompleted = it)))
                },
            )
        }
    }
}

@PreviewScreenSizes
@PreviewLightDark
@Composable
private fun HomeContentPreview() {
    MyTasksTheme {
        HomeContent(
            uiState =
                HomeState(
                    tasks =
                        listOf(
                            Task(
                                title = "Grocery Shopping",
                                dueDate = System.currentTimeMillis(),
                            ),
                            Task(
                                title = "Book Appointment",
                                dueDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1),
                            ),
                            Task(title = "Pay Bills", dueDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(2)),
                            Task(
                                title = "Schedule Meeting",
                                dueDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(3),
                            ),
                        ),
                ),
            onEvent = {},
        )
    }
}
