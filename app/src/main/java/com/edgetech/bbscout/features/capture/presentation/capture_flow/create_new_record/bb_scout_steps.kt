package com.edgetech.bbscout.features.capture.presentation.capture_flow.create_new_record

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp


@Composable
fun BBScoutStepper(
    numberOfSteps: Int,
    currentStep: Int,
    modifier: Modifier = Modifier
){
    Surface(
        modifier = modifier.height(4.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.small
    ) {
        Row {
            for (i in 1..numberOfSteps){
                Stepper(isComplete = i <= currentStep, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun Stepper(
isComplete: Boolean,
modifier: Modifier = Modifier
){
    Surface(
        modifier = modifier.fillMaxHeight(),
        color = if(isComplete) MaterialTheme.colorScheme.primary else Color.Transparent,
        shape = RectangleShape
    ) {

    }
}