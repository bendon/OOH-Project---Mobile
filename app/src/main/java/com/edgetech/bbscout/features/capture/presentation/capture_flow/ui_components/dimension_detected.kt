package com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components

import androidx.annotation.Dimension
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edgetech.bbscout.components.ui.ButtonContent
import com.edgetech.bbscout.components.ui.MainLoadingButton


@Composable
fun DimensionDetectedComp(
    unit: String,
    width: Double?,
    height: Double?,
    onConfirm: (width: Double?, height: Double?) -> Unit,
    modifier: Modifier = Modifier
){

    val widthTextState = rememberTextFieldState(initialText = width?.toString() ?: "")
    val heightTextState = rememberTextFieldState(initialText = height?.toString() ?: "")




    InfoComposableContainer(InfoComposableType.ALL_GRAY, modifier = modifier) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Dimensions detected",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                DimensionDetectedMeasurement(
                    title = "Width: ",
                    unit = unit,
                    textFieldState = widthTextState,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                DimensionDetectedMeasurement(
                    title = "Height: ",
                    unit = unit,
                    textFieldState = heightTextState,
                    modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth()
                )
                MainLoadingButton(onTap = {
                    onConfirm(widthTextState.text.toString().toDoubleOrNull(), heightTextState.text.toString().toDoubleOrNull())
                },
                    loadOnTap = false,
                    disableOnTap = false,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 24.dp)
                    ) {
                    ButtonContent(
                        title = "Confirm dimension",
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                }

            }
    }


}

@Composable
fun DimensionDetectedMeasurement(
    title: String,
    unit: String,
    textFieldState: TextFieldState,
    modifier: Modifier = Modifier
){
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            state = textFieldState,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.padding(start = 8.dp, end = 4.dp).width(70.dp).height(30.dp),
            contentPadding  = PaddingValues(2.dp),
        )
        Text(
            text = unit,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Normal,
            modifier = Modifier
        )
    }

}


@Preview
@Composable
fun DimensionDetectedCompPreview(){
    DimensionDetectedComp(unit = "m", width = 10.0, height = 20.0, onConfirm = { _, _ -> })
}