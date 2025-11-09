package dev.alimansour.mytasks.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.alimansour.mytasks.core.domain.model.Task
import dev.alimansour.mytasks.core.ui.utils.formatDueDate
import dev.alimansour.mytasks.ui.theme.interFamily

@Composable
fun TaskItem(
    task: Task,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!task.isCompleted) }
                .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = onCheckedChange,
            colors =
                CheckboxDefaults.colors(
                    checkedColor = Color(0xFF3DDC84),
                    uncheckedColor = Color.LightGray,
                ),
        )

        Spacer(Modifier.width(16.dp))

        Column {
            Text(
                text = task.title,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = interFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = context.formatDueDate(task.dueDate),
                color = MaterialTheme.colorScheme.primary,
                fontFamily = interFamily,
                fontSize = 14.sp,
            )
        }
    }
}
