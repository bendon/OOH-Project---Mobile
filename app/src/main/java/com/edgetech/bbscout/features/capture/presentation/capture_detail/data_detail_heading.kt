package com.edgetech.bbscout.features.capture.presentation.capture_detail

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edgetech.bbscout.R
import com.edgetech.bbscout.features.capture.presentation.review_data.RecordType
import com.edgetech.bbscout.features.navigation.AppDestinations


@Composable
fun DataDetailHeading(
   @DrawableRes icon: Int?,
   title: String,
   hasEditIcon: Boolean = false,
   onEditClick: () -> Unit = {},
   modifier: Modifier = Modifier.fillMaxWidth(),
){
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (icon != null)
        Surface(
            shape = CircleShape,
            modifier = Modifier.size(28.dp)
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp),
                colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primary)
            )
        }
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 8.dp)
                .weight(1f)
        )
        if (hasEditIcon)
        Icon(Icons.Outlined.Edit, "", tint = Color.Gray, modifier = Modifier.clickable {
            onEditClick()
        })
    }
}