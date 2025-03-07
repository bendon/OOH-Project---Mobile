package com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.edgetech.bbscout.ui.theme.lightBlue
import com.edgetech.bbscout.ui.theme.mainBlue
import com.edgetech.bbscout.ui.theme.mainGreen


@Composable
fun InfoComposableContainer(
    type: InfoComposableType,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,

    ){
    Surface(
        modifier = modifier,
        color = when(type){
            InfoComposableType.GREEN -> {
                mainGreen.copy(alpha = 0.2f)
            }
            InfoComposableType.BLUE -> {
                lightBlue
            }
            InfoComposableType.ALL_GRAY -> {
                MaterialTheme.colorScheme.surface
            }
            InfoComposableType.BORDER_GRAY -> {
                MaterialTheme.colorScheme.background
            }
            InfoComposableType.RED -> {
                Color.Red.copy(alpha = 0.2f)
            }
        },
        border = BorderStroke(
            width = 2.dp,
            color = when(type){
                InfoComposableType.GREEN -> {
                    mainGreen
                }
                InfoComposableType.BLUE -> {
                    mainBlue
                }
                InfoComposableType.ALL_GRAY -> {
                    MaterialTheme.colorScheme.surface
                }
                InfoComposableType.BORDER_GRAY -> {
                    MaterialTheme.colorScheme.surface
                }
                InfoComposableType.RED -> {
                    Color.Red
                }
            }
        ),
        shape = MaterialTheme.shapes.small
    ) {
        content()
    }
}

enum class InfoComposableType{
    GREEN,
    BLUE,
    ALL_GRAY,
    BORDER_GRAY,
    RED
}