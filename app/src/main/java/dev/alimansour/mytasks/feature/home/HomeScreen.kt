package dev.alimansour.mytasks.feature.home

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.alimansour.mytasks.R
import dev.alimansour.mytasks.core.domain.model.Task
import dev.alimansour.mytasks.core.ui.common.CommonTopAppBar
import dev.alimansour.mytasks.core.ui.common.LaunchedUiEffectHandler
import dev.alimansour.mytasks.core.ui.navigation.Route
import dev.alimansour.mytasks.core.ui.theme.MyTasksTheme
import dev.alimansour.mytasks.core.ui.utils.UiText
import org.koin.androidx.compose.koinViewModel
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    navigateToRoute: (Route) -> Unit,
    navigateToTaskDetails: (Task) -> Unit,
    onFabClick: () -> Unit,
    showError: (message: UiText) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context: Context = LocalContext.current

    BackHandler {
        viewModel.processEvent(HomeEvent.OnBackPress)
    }

    LaunchedUiEffectHandler(
        viewModel.effect,
        onConsumeEffect = { viewModel.processEvent(HomeEvent.ConsumeEffect) },
        onEffect = { effect ->
            when (effect) {
                is HomeEffect.NavigateToRoute -> {
                    navigateToRoute(effect.route)
                }

                is HomeEffect.NavigateToTaskDetails -> {
                    navigateToTaskDetails(effect.task)
                }

                is HomeEffect.ShowError -> {
                    showError(effect.message)
                }
            }
        },
    )

    Scaffold(
        topBar = {
            CommonTopAppBar(title = stringResource(id = R.string.app_name), showBackIcon = false) { }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onFabClick() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                expanded = uiState.isFabExpanded,
                text = { Text(text = stringResource(R.string.add_task)) },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = stringResource(R.string.add_task),
                    )
                },
            )
        },
    ) { paddingValues ->
        HomeContent(
            uiState = uiState,
            modifier = Modifier.padding(paddingValues),
            onEvent = viewModel::processEvent,
        )
    }
}

@Composable
private fun HomeContent(
    uiState: HomeState,
    modifier: Modifier = Modifier,
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
        modifier = modifier.fillMaxSize(),
        state = listState,
    ) {
        items(uiState.tasks, key = { task ->
            task.id
        }) { task ->
            TaskItem(
                task = task,
                onCheckedChange = {
                    onEvent(HomeEvent.OnTaskCheckChanged(task.copy(isCompleted = it)))
                },
                onItemClick = { onEvent(HomeEvent.NavigateToTaskDetailsScreen(task)) },
            )
        }
    }
}

@PreviewScreenSizes
@PreviewLightDark
@Composable
private fun HomeContentPreview() {
    MyTasksTheme(dynamicColor = false) {
        HomeContent(
            uiState =
                HomeState(
                    tasks =
                        listOf(
                            Task(
                                id = 1,
                                title = "Grocery Shopping",
                                dueDate = System.currentTimeMillis(),
                            ),
                            Task(
                                id = 2,
                                title = "Book Appointment",
                                dueDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1),
                            ),
                            Task(id = 3, title = "Pay Bills", dueDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(2)),
                            Task(
                                id = 4,
                                title = "Schedule Meeting",
                                dueDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(3),
                            ),
                        ),
                ),
            onEvent = {},
        )
    }
}
