package com.edgetech.bbscout.features.capture.presentation.capture_flow.select_billboard_type

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diracks.app.app.app_state.BBScoutAppState
import com.edgetech.bbscout.R
import com.edgetech.bbscout.features.capture.domain.model.BillboardSides
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordEventSink
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiModel
import com.edgetech.bbscout.features.capture.domain.viewmodel.CaptureRecordViewmodel
import com.edgetech.bbscout.ui.theme.BBScoutTheme


@Composable
fun SelectCaptureTypeMain(
    appState: BBScoutAppState?,
    captureRecordUiModel: CaptureRecordUiModel
) {

    var selectedType by rememberSaveable {
        mutableStateOf<String>("")
    }

    var selectedNumberOfSide by rememberSaveable {
        mutableStateOf<BillboardSides>(BillboardSides.SIDE_ONE)
    }

    LaunchedEffect(
        selectedType,
        selectedNumberOfSide
    ) {
        captureRecordUiModel.captureEventSink(
            CaptureRecordEventSink.StartBillBoardSurvey(selectedNumberOfSide, selectedType)
        )
    }

    Column(
        modifier = Modifier.padding(16.dp)
    ) {

        BoxWithConstraints {
            LazyVerticalGrid(
                columns = if (maxWidth < 600.dp) {
                    androidx.compose.foundation.lazy.grid.GridCells.Fixed(2)
                } else {
                    androidx.compose.foundation.lazy.grid.GridCells.Fixed(4)
                },

                ) {
                item(
                    span = {
                        GridItemSpan(maxLineSpan)
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "Select signage type",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            minLines = 1,
                            maxLines = 1,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            text = "Select the type of signage you are capturing to optimize detection",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium,
                            minLines = 3,
                            maxLines = 3,
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .fillMaxWidth(),
                        )
                    }
                }
                items(BillboardType.entries.size) {
                    BillboardTypeComp(
                        name = BillboardType.entries[it].displayName,
                        description = BillboardType.entries[it].description,
                        itemIcon = BillboardType.entries[it].icon,
                        isSelected = selectedType == it.toString(),
                        onClick = {
                            selectedType = BillboardType.entries[it].displayName
                        },
                        modifier = Modifier.padding(4.dp)
                    )
                }
                item(
                    span = {
                        GridItemSpan(maxLineSpan)
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "Number of sides",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            minLines = 1,
                            maxLines = 1,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        )
                        Text(
                            text = "Select how many sides you of the signage you are capturing",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .fillMaxWidth(),
                        )
                        Row(
                            modifier = Modifier.padding(top = 16.dp)
                                .horizontalScroll(
                                    rememberScrollState()
                                )
                        ) {
                            BillboardSideCountComp(
                                text = "1 side",
                                isSelected = selectedNumberOfSide == BillboardSides.SIDE_ONE,
                                onClick = {
                                    selectedNumberOfSide = BillboardSides.SIDE_ONE
                                },
                                modifier = Modifier.padding(end = 16.dp)
                            )
                            BillboardSideCountComp(
                                text = "2 sides",
                                isSelected = selectedNumberOfSide == BillboardSides.SIDE_TWO,
                                onClick = {
                                    selectedNumberOfSide = BillboardSides.SIDE_TWO
                                },
                                modifier = Modifier.padding(end = 16.dp)
                            )
                            BillboardSideCountComp(
                                text = "3 sides",
                                isSelected = selectedNumberOfSide == BillboardSides.SIDE_THREE,
                                onClick = {
                                    selectedNumberOfSide = BillboardSides.SIDE_THREE
                                },
                                modifier = Modifier.padding(end = 16.dp)
                            )
                            BillboardSideCountComp(
                                text = "4 sides",
                                isSelected = selectedNumberOfSide == BillboardSides.SIDE_FOUR,
                                onClick = {
                                    selectedNumberOfSide = BillboardSides.SIDE_FOUR
                                },
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun BillboardSideCountComp(
    text: String,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = isSelected, onClick = onClick, modifier = Modifier.size(20.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(start = 8.dp)
            )
    }
}

enum class  BillboardType(val displayName: String, val description: String, val icon: Int){

    StaticBillboard("Static Billboard", "Large outdoor advertising structure", R.drawable.ic_billboard),
    DigitalBillboard("Digital Billboard", "Electronic display for advertising", R.drawable.ic_billboard),
    BannerAds("Banner Ads", "Rectangular advertisements on websites", R.drawable.ic_billboard),
    Wallscapes("Wallscapes", "Large murals or paintings on walls", R.drawable.ic_billboard),
    MobileBillboards("Mobile Billboards", "Advertising vehicles on wheels", R.drawable.ic_billboard),
    LampPosts("Lamp Posts", "Advertising affixed to streetlights", R.drawable.ic_billboard),
    InteractiveBillboards("Interactive Billboards", "Billboards with interactive features", R.drawable.ic_billboard);




}

@Preview
@Composable
fun SelectCaptureTypeMainPreview() {
    BBScoutTheme {
        SelectCaptureTypeMain(null, CaptureRecordUiModel())
    }

}