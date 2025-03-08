package com.edgetech.bbscout.features.capture.presentation.capture_detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun DataDetailMultipleValues(
    title: String,
    values: List<String>?,
    modifier: Modifier = Modifier
        .padding(top = 6.dp)
        .fillMaxWidth()
){
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(end = 16.dp)
        )
        Column(
            horizontalAlignment = Alignment.End
        ) {
            values?.forEach {
                Text(
                    text = it,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            if (values.isNullOrEmpty()){
                Text(
                    text = "N/A",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                )
            }
        }
    }
}