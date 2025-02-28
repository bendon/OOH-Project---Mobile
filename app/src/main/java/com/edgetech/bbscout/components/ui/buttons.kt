package com.edgetech.bbscout.components.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.edgetech.bbscout.ui.theme.lightBlue
import com.edgetech.bbscout.ui.theme.mainBlue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds


@Composable
fun ButtonContent(
    title: String,
    contentColor: Color,
    icons: ImageVector? = null,
    brush: Brush? = null

) {
    if (icons != null) {
        Icon(
            icons,
            contentDescription = title,
            modifier = Modifier.padding(horizontal = 4.dp),
            tint = contentColor
        )
    }
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(brush = brush),
        modifier = Modifier.padding(horizontal = 4.dp),
        color = contentColor
    )
}

@Composable
fun NonLoadingSecButton(
    modifier: Modifier = Modifier,
    disableOnTap: Boolean = true,
    timeOut: Duration = 3.seconds,
    onTap: () -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    LoadingButton(
        modifier = modifier
            .sizeIn(minHeight = 40.dp),
        loadOnTap = false,
        disableOnTap = disableOnTap,
        timeOut = timeOut,
        onTap = onTap,
        content = content,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.background,
            disabledContainerColor = lightBlue
        ),
        shape = RoundedCornerShape(20),
        border = BorderStroke(width = 1.dp, color = lightBlue)
    )
}
@Composable
fun MainLoadingButton(
    modifier: Modifier = Modifier,
    pIsLoading: Boolean = false,
    loadOnTap: Boolean = true,
    disableOnTap: Boolean = true,
    timeOut: Duration = 1.minutes,
    onTap: () -> Unit,
    content: @Composable RowScope.() -> Unit,
) {

        LoadingButton(
            modifier = modifier//.clip(RoundedCornerShape(8)),
                .sizeIn(minHeight = 40.dp),
            loadOnTap = loadOnTap,
            timeOut = timeOut,
            pIsLoading = pIsLoading,
            disableOnTap = disableOnTap,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(20),
            onTap = onTap,
            content = content
        )

}
@Composable
fun LoadingButton(
    modifier: Modifier = Modifier,
    pIsLoading: Boolean = false,
    loadOnTap: Boolean = true,
    disableOnTap: Boolean = true,
    timeOut: Duration = 2.minutes,
    onTap: () -> Unit,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,

    ) {

    var isLoading: Boolean by remember { mutableStateOf(pIsLoading) }
    isLoading = pIsLoading
    var enabledButton: Boolean by remember { mutableStateOf(!pIsLoading) }
    enabledButton = !pIsLoading
    //logD("Loading is $isLoading")
    if (disableOnTap) {
        LaunchedEffect(enabledButton) {
            withContext(Dispatchers.IO) {
                if (enabledButton) return@withContext
                else delay(2.seconds)
                enabledButton = true
            }
        }
    }

    LaunchedEffect(isLoading) {
        if (!isLoading) return@LaunchedEffect
        else {
            delay(timeOut)
            isLoading = false
        }

    }

    Button(
        modifier = modifier,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        onClick = {
            onTap()
            if (loadOnTap) {
                isLoading = true
            }
            if (!isLoading && disableOnTap)
                enabledButton = false

        },
        enabled = enabledButton
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(vertical = 4.dp).size(28.dp),
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            content()
        }

    }


}