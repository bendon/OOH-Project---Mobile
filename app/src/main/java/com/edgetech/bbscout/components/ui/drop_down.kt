package com.edgetech.bbscout.components.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.edgetech.bbscout.ui.theme.BBScoutTheme
import com.google.android.material.color.MaterialColors.ALPHA_DISABLED
import com.google.android.material.color.MaterialColors.ALPHA_FULL

/**
 *Inspired by https://proandroiddev.com/improving-the-compose-dropdownmenu-88469b1ef34
 *
 * From Peter Törnhult
 *
 * Fetched and modified on 6/6/2024
 */

@Composable
fun LargeDropdownMenu(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String,
    notSetLabel: String? = null,
    items: List<String>,
    selectedIndex: Int = -1,
    onItemSelected: (index: Int, item: String) -> Unit,
    drawItem: @Composable (String, Boolean, Boolean, () -> Unit) -> Unit = { item, selected, itemEnabled, onClick ->
        LargeDropdownMenuItem(
            text = item,
            selected = selected,
            enabled = itemEnabled,
            onClick = onClick,
        )
    },
) {
    var expanded by remember { mutableStateOf(false) }

    val editTextState = rememberTextFieldState(
        initialText = items.getOrNull(selectedIndex) ?: "",
    )

    editTextState.setTextAndPlaceCursorAtEnd(items.getOrNull(selectedIndex) ?: "")

    Box(modifier = modifier.height(IntrinsicSize.Min)) {
        Column {
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .padding(bottom = 2.dp, top = 8.dp)
                    .align(Alignment.Start)
            )
            OutlinedTextField(
                placeholder = { Text(label) },
                state = editTextState,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                ),
                contentPadding = PaddingValues(14.dp),
                trailingIcon =
                {
                    val icon =
                        if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown
                    Icon(icon, "")
                },
                readOnly = true,
            )
        }

        // Transparent clickable surface on top of OutlinedTextField
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .clickable(enabled = enabled) { expanded = true },
            color = Color.Transparent,
        ) {}
    }

    if (expanded) {
        Dialog(
            onDismissRequest = { expanded = false },
        ) {
            BBScoutTheme  {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.background
                ) {

                    var searchText by rememberSaveable { mutableStateOf("") }
                    var filteredItems by rememberSaveable { mutableStateOf(items) }
                    val listState = rememberLazyListState()



                    if (selectedIndex > -1) {
                        LaunchedEffect("ScrollToSelected") {
                            listState.scrollToItem(index = selectedIndex)
                        }
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            label = { Text(label,  maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            value = searchText,
                            enabled = enabled,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            onValueChange = { it ->
                                searchText = it
                                filteredItems = if (searchText.isNotEmpty()) {
                                    items.filter {
                                        it.lowercase().contains(searchText.lowercase())
                                    }.sorted()
                                } else {
                                    items.sorted()
                                }

                            },
                        )
                        LazyColumn(modifier = Modifier.fillMaxWidth(), state = listState) {


                            if (notSetLabel != null) {
                                item {
                                    LargeDropdownMenuItem(
                                        text = notSetLabel,
                                        selected = false,
                                        enabled = false,
                                        onClick = { },
                                    )
                                }
                            }
                            itemsIndexed(filteredItems) { index, item ->
                                val selectedItem = index == selectedIndex
                                drawItem(
                                    item,
                                    selectedItem,
                                    true
                                ) {
                                    val originalIndex = items.indexOf(item)
                                    onItemSelected(originalIndex, item)
                                    expanded = false
                                }

                                if (index < items.lastIndex) {
                                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LargeDropdownMenuItem(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val contentColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = ALPHA_DISABLED)
        selected -> MaterialTheme.colorScheme.primary.copy(alpha = ALPHA_FULL)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = ALPHA_FULL)
    }

  CompositionLocalProvider(LocalContentColor provides contentColor) {
        Box(modifier = Modifier
            .clickable(enabled) { onClick() }
            .fillMaxWidth()
            .padding(16.dp)) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
            )
        }
    }
}


@Preview
@Composable
fun LargeDropdownMenuPreview(){
    val animals = listOf(
        "Lion",
        "Tiger",
        "Leopard",
        "Cheetah",
        "Giraffe",
        "Elephant",
        "Zebra",
        "Kangaroo",
        "Koala",
        "Panda",
        "Gorilla",
        "Hippopotamus",
        "Rhinoceros",
        "Orangutan",
        "Polar Bear",
        "Grizzly Bear",
        "Sloth",
        "Kangaroo",
        "Koala",
        "Panda",
        "Gorilla",
        "Hippopotamus",
        "Rhinoceros",
        "Orangutan",)
    var selectedIndex by remember { mutableStateOf(-1) }
    LargeDropdownMenu(
        modifier = Modifier.padding(16.dp),
        label = "Sample",
        items = animals,
        selectedIndex = selectedIndex,
        onItemSelected = { index, _ -> selectedIndex = index },
    )
}