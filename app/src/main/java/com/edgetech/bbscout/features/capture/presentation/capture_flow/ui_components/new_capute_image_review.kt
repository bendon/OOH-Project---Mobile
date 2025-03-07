package com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diracks.app.app.app_state.BBScoutAppState
import com.edgetech.bbscout.R
import com.edgetech.bbscout.ui.theme.BBScoutTheme
import com.edgetech.bbscout.ui.theme.mainGreen


@Composable
fun NewCaptureImageReview(
    image: Bitmap?,
    title: String,
    status: String = "Captured",
    modifier: Modifier = Modifier,
    onTap: () -> Unit = {},
){
    InfoComposableContainer(
        type = InfoComposableType.BORDER_GRAY,
        modifier = modifier.clickable {
            onTap()
        }
    ) {
        Column {
            if (image != null) {
                Image(bitmap = image.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxWidth().height(170.dp))
            } else {
                Image(painter = painterResource(R.drawable.no_image), contentDescription = null, modifier = Modifier.fillMaxWidth().height(170.dp))
            }
            HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.surface)
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .weight(1f),
                )
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.check_circle_svgrepo_com),
                        contentDescription = null,
                        colorFilter =  ColorFilter.tint(mainGreen),
                        modifier = Modifier.padding(end = 2.dp).size(16.dp)
                    )
                    Text(
                        text = status,
                        fontSize = 16.sp, fontWeight = FontWeight.Bold,
                        color =  mainGreen
                    )

                }
            }
        }
    }
}

@Preview
@Composable
fun NewCaptureImageReviewPreview(){
    BBScoutTheme {
        NewCaptureImageReview(image = null, title = "Front view")
    }

}