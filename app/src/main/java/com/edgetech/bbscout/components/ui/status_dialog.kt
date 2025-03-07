package com.edgetech.bbscout.components.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.edgetech.bbscout.R
import com.edgetech.bbscout.ui.theme.mainBlue
import kotlin.time.Duration.Companion.seconds

@Composable
fun StatusDialog(
    isVisible: Boolean,
    onDismissRequest: () -> Unit,
    onConfirmation: (() -> Unit)? = null,
    title: String,
    message: String,
    posText: String? = null,
    negText: String? = null,
    icon: (@Composable () -> Unit)? = null,
    // @RawRes statusType: Int
) {

    //  val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(statusType))


    if (isVisible) {
        Dialog(onDismissRequest = { onDismissRequest() }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = title, style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp),
                    )

//                LottieAnimation(
//                    modifier = Modifier
//                        .wrapContentHeight() ,
//                    composition = composition,
//                    iterations = 1,
//                    clipSpec = clipSpecs,
//                )
                    if (icon != null) {
                        icon()
                    }

                    Text(
                        text = message,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp),
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (negText != null) {

                            NonLoadingSecButton(
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .weight(1f),
                                onTap = {
                                    onDismissRequest()
                                }) {
                                ButtonContent(
                                    negText,
                                    MaterialTheme.colorScheme.onBackground,
                                    // brush = LocalAppResources.current.primaryBackgroundBrush
                                )

                            }
                        }
                    if (posText != null) {

                        Box(
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .weight(1f)
                        ) {
                            MainLoadingButton(
                                onTap = {
                                    onDismissRequest()
                                    onConfirmation?.invoke()
                                },
                                modifier = Modifier
                                    //.padding(top = 16.dp, start = 16.dp, end = 16.dp)
                                    .fillMaxWidth(),
                                loadOnTap = false,
                                timeOut = 2.seconds
                            ) {
                                Text(text = posText, color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }

                    }


                    }
                    Spacer(modifier = Modifier.height(16.dp))



                }
            }
        }
    } else return
}
@Composable
fun ShowErrorDialog(
    isVisible: Boolean,
    onDismissRequest: () -> Unit,
    onConfirmation: (() -> Unit)? = null,
    title: String,
    message: String,
    posText: String? = null,
    negText: String? = null,
){
    StatusDialog(
        isVisible = isVisible,
        onDismissRequest = { onDismissRequest() },
        onConfirmation = { onConfirmation?.invoke() },
        title = title,
        message = message,
        posText = posText,
        negText = negText,
        icon = {
            ErrorIcon()
        }
    )
}

@Composable
fun SuccessIcon() {
    Image(
        painterResource(R.drawable.check_circle_svgrepo_com),
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
        contentDescription = "Success",
        modifier = Modifier.size(78.dp)
    )
}

@Composable
fun ErrorIcon() {

    Icon(
        Icons.Outlined.Error,
        //tint = monzaDark,
        contentDescription = "Error",
        modifier = Modifier.size(78.dp)
    )
}

@Composable
fun WarningIcon() {

    Icon(
        Icons.Outlined.Warning,
        tint = Color(0xffffcc00),
        contentDescription = "Warning",
        modifier = Modifier.size(78.dp)
    )
}

@Composable
fun HelpIcon() {

    Icon(
        Icons.Outlined.Help,
       // tint = LocalAppResources.current.successColor,
        contentDescription = "Help",
        modifier = Modifier.size(78.dp)
    )
}