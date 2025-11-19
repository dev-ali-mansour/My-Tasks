package dev.alimansour.mytasks.core.ui.view

import android.R
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    onDismissRequest: () -> Unit,
    onConfirmed: () -> Unit,
    onCancelled: () -> Unit,
) {
    AlertDialog(
        modifier = modifier,
        containerColor = backgroundColor,
        onDismissRequest = { onDismissRequest() },
        title = {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontStyle = MaterialTheme.typography.labelMedium.fontStyle,
                overflow = TextOverflow.Ellipsis,
            )
        },
        text = {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurface,
                fontStyle = MaterialTheme.typography.bodyLarge.fontStyle,
                overflow = TextOverflow.Ellipsis,
            )
        },
        confirmButton = {
            Button(onClick = { onConfirmed() }) {
                Text(
                    text = stringResource(id = R.string.ok),
                    color = Color.White,
                )
            }
        },
        dismissButton = {
            Button(onClick = { onCancelled() }) {
                Text(
                    text = stringResource(id = R.string.cancel),
                    color = Color.White,
                )
            }
        },
    )
}
