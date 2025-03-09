package com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edgetech.bbscout.R
import com.edgetech.bbscout.ui.theme.mainBlue
import com.edgetech.bbscout.ui.theme.mainGreen


@Composable
fun InPageAlert(
    isSuccess: Boolean,
    isError: Boolean,
    title: String?,
    message: String?,
    onRetry: (() -> Unit)? = null,
    onNext: (() -> Unit)? = null,
    retryText: String = "Retry",
    nextText: String = "Next",
    modifier: Modifier = Modifier
) {
    InfoComposableContainer(
        type = if (isSuccess) InfoComposableType.GREEN else if (isError) InfoComposableType.RED else InfoComposableType.BLUE,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row {
                if (isSuccess)
                    Image(
                        painterResource(
                            R.drawable.check_circle_svgrepo_com
                        ),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(mainGreen),
                        modifier = Modifier.size(24.dp)
                    )
                else
                    Icon(
                        imageVector = if (isError) Icons.Outlined.Error else Icons.Outlined.Info,
                        contentDescription = null,
                        tint = if (isError) Color.Red else mainBlue,
                        modifier = Modifier.size(24.dp)
                    )
                Column(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f),

                    ) {
                    if (!title.isNullOrEmpty())
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSuccess) mainGreen else if (isError) Color.Red else mainBlue,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    if (!message.isNullOrEmpty())
                        Text(
                            text = message,
                            fontSize = 14.sp,
                            color = if (isSuccess) mainGreen else if (isError) Color.Red else mainBlue,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .fillMaxWidth(),
                        )

                }


            }

            if (onRetry != null) {
                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = mainGreen
                    ),
                    border = BorderStroke(1.dp, mainGreen)
                ) {
                    Text(text = retryText)
                }
            }
            if (onNext != null) {
                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSuccess) mainGreen else MaterialTheme.colorScheme.primary,
                        contentColor = if (isSuccess) Color.White else MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(text = nextText)
                }
            }
        }
    }

}

@Preview
@Composable
fun InPageAlertPreview() {
    InPageAlert(
        isSuccess = false,
        isError = true,
        title = "Alert Title",
        message = "Alert Message",
        // onRetry = {},
    )
}