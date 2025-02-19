package com.edgetech.bbscout.features.capture.presentation.review_data

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.diracks.app.app.app_state.BBScoutAppState
import com.edgetech.bbscout.features.navigation.AppDestinations


@Composable
fun OnDataAdded(
    appState: BBScoutAppState?
) {

    Scaffold {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Outlined.CheckCircle,
                contentDescription = "Data Added",
                tint = Color.Green,
                modifier = Modifier.size(120.dp)
            )
            Text(
                "Billboard Captured",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Button(
                onClick = {
                    appState?.navController?.popBackStack(AppDestinations.Dashboard, false)
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
            ) {
                Icon(Icons.Outlined.Close, contentDescription = "Capture")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Finish")
            }
        }
    }


}