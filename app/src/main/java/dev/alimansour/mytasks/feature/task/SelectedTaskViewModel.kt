package dev.alimansour.mytasks.feature.task

import androidx.lifecycle.ViewModel
import dev.alimansour.mytasks.core.domain.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class SelectedTaskViewModel : ViewModel() {
    private val _selectedTask = MutableStateFlow<Task?>(null)
    val selectedTask = _selectedTask.asStateFlow()

    fun onSelectTask(task: Task?) {
        _selectedTask.value = task
    }
}
