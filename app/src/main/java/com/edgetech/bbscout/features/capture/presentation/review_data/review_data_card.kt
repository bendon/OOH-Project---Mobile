package com.edgetech.bbscout.features.capture.presentation.review_data

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ReviewDataCard(
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 8.dp)
        .padding(top = 16.dp),
    content: @Composable () -> Unit,
){
    Card(
        shape = MaterialTheme.shapes.small,
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
    ) {
        content()

    }
}