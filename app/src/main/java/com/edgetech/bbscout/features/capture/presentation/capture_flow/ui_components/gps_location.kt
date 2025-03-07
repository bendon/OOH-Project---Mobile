package com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.edgetech.bbscout.ui.theme.lightBlue


@Composable
fun GpsLocationComp(
    lat: Double? = null,
    lng: Double? = null,
    modifier: Modifier = Modifier
){
    Surface(
        shape = MaterialTheme.shapes.small,
        color = lightBlue,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.GpsFixed, contentDescription = null)
            Column(
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text(
                    text = "GPS location", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = if (lat != null && lng != null) "Lat: $lat, Lng: $lng" else "Waiting for GPS location...",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .fillMaxWidth(),
                )
            }
        }
    }
}

@Preview
@Composable
fun GpsLocationCompPreview(){
    GpsLocationComp()
}