package com.edgetech.bbscout.features.capture.presentation.capture_detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edgetech.bbscout.components.utils.ifEmptySetNull


@Composable
fun DetailDataOneValue(
    title: String,
    value: String,
    modifier: Modifier = Modifier
        .padding(top = 6.dp)
){
    Row(
        modifier = Modifier
            .padding(top = 6.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(end = 16.dp)
        )
        Text(
            text = value ,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}