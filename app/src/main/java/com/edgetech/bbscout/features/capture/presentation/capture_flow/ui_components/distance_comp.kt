package com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edgetech.bbscout.ui.theme.mainGreen


@Composable
fun DistanceComposable(
    title: String,
    distance: Double?,
    unit: String = "m",
    maxDistance: Double? = null,
    minDistance: Double? = null,
    isValid: Boolean? = null,
    modifier: Modifier = Modifier
){

    InfoComposableContainer(InfoComposableType.ALL_GRAY, modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth(),
            )

           Row(
               modifier = Modifier.padding(top = 8.dp),
               verticalAlignment = Alignment.CenterVertically
           ) {
               Icon(
                   if (isValid == true) Icons.Outlined.Check else Icons.Outlined.WarningAmber,
                   contentDescription = null,
                   tint = if (isValid == true) mainGreen else Color.Red,
                   modifier = Modifier.padding(end = 4.dp).size(16.dp)
               )
               Text(
                   text = "Distance: ${distance ?: "0"} $unit(Target: ${minDistance ?: "0"}-${maxDistance ?: 0}$unit)",
                   fontSize = 16.sp, fontWeight = FontWeight.Bold,
                   modifier = Modifier.fillMaxWidth(),
                   color = if (isValid == true) mainGreen else Color.Red
               )

           }

        }


    }

}

@Preview
@Composable
fun DistanceComposablePreview(){
    DistanceComposable(title = "Distance to billboard", distance = 10.0, maxDistance = 20.0, minDistance = 5.0, isValid = false)
}