package com.edgetech.bbscout.features.capture.presentation.capture_flow.select_billboard_type

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.InfoComposableContainer
import com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components.InfoComposableType


@Composable
fun BillboardTypeComp(
    @DrawableRes itemIcon: Int,
    name: String,
    description: String,
    onClick: () -> Unit,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    InfoComposableContainer(
        type = if (isSelected) InfoComposableType.BLUE else InfoComposableType.BORDER_GRAY,
        modifier = modifier.clickable {
            onClick()
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(itemIcon),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .padding(bottom = 8.dp)
            )
            Text(
                text = name, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                minLines = 1,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium,
                minLines = 3,
                maxLines = 3,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )

        }
    }
}