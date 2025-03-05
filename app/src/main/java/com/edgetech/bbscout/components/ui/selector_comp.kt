package com.edgetech.bbscout.components.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp


@Composable
fun SelectorComposable(
    modifier: Modifier = Modifier,
    onTap: (() -> Unit)? = null,
    status: CompSelectableState = CompSelectableState.NOT_SELECTED,
    content: String,
    showRadioBtn: Boolean = false
) {
    SelectorComposable(modifier = modifier, onTap = onTap, status = status) {
        if (showRadioBtn) {
            RadioButton(selected = status == CompSelectableState.SELECTED, onClick = {
                onTap?.invoke()
            }, modifier = Modifier
                .size(28.dp)
                .padding(horizontal = 16.dp))
        }
        Text(text = content, style = MaterialTheme.typography.titleMedium, color = if(status != CompSelectableState.CLICKABLE) Color.Gray else Color.White, modifier = Modifier.padding(start = if (showRadioBtn)  16.dp else 0.dp).fillMaxWidth(), textAlign = TextAlign.Center)
    }
}


@Composable
fun SelectorComposable(
    modifier: Modifier = Modifier,
    onTap: (() -> Unit)? = null,
    status: CompSelectableState = CompSelectableState.NOT_SELECTED,
    content: @Composable RowScope.() -> Unit,
) {

    val containerColor = when (status) {
        CompSelectableState.CLICKABLE -> MaterialTheme.colorScheme.background
        CompSelectableState.NOT_SELECTED -> Color.Transparent
        CompSelectableState.SELECTED -> MaterialTheme.colorScheme.primaryContainer
    }

    val borderColor = when (status) {
        CompSelectableState.CLICKABLE -> MaterialTheme.colorScheme.secondary
        CompSelectableState.NOT_SELECTED -> MaterialTheme.colorScheme.surface
        CompSelectableState.SELECTED -> MaterialTheme.colorScheme.primary
    }

    val contentColor = when (status) {
        CompSelectableState.CLICKABLE -> Color.White
        CompSelectableState.NOT_SELECTED -> MaterialTheme.colorScheme.onBackground
        CompSelectableState.SELECTED -> MaterialTheme.colorScheme.onBackground
    }


    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        onClick = { onTap?.invoke() },
        border = BorderStroke(1.dp, borderColor),
        color = containerColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }

}



enum class CompSelectableState {
    CLICKABLE,
    NOT_SELECTED,
    SELECTED

}



