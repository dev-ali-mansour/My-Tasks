package dev.alimansour.mytasks.core.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import dev.alimansour.mytasks.R
import dev.alimansour.mytasks.core.ui.theme.MyTasksTheme
import dev.alimansour.mytasks.core.ui.theme.interFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonTopAppBar(
    title: String,
    showBackIcon: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {},
    onNavigationIconClicked: () -> Unit,
) {
    Column {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = title,
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
            navigationIcon = {
                if (showBackIcon) {
                    IconButton(onClick = {
                        onNavigationIconClicked()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = stringResource(R.string.close),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            },
            actions = actions,
        )
    }
}

@PreviewLightDark
@Composable
private fun SplashScreenPreview() {
    MyTasksTheme(dynamicColor = false) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            CommonTopAppBar(title = stringResource(R.string.app_name)) {
            }
        }
    }
}
