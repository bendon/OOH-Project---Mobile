package com.edgetech.bbscout.features.settings.presentation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun SettingsItemComp(
    name: String,
    @DrawableRes fileItem: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasDivider: Boolean = true
){
    Column(
        modifier = modifier.padding(vertical = 4.dp).clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
        ) {

            Surface(
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Image(
                    painter = painterResource(fileItem),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .size(32.dp)
                        .padding(12.dp)
                )
            }
//            Column(
//                modifier = Modifier
//                    .padding(start = 16.dp)
//                    .weight(1f)
//            ) {
                Text(text = name, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).padding(horizontal = 8.dp) )

          //  }
            Icon(
                Icons.Outlined.ArrowForwardIos,
                null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(16.dp)
            )
        }
    }
    if (hasDivider)
        HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.surface)
}