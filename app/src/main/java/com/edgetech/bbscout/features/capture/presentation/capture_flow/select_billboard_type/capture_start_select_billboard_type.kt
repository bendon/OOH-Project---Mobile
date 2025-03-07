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
        mutableStateOf(1)
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
                items(8) {
                    BillboardTypeComp(
                        name = "Billboard",
                        description = "Large outdoor advertising structure",
                        itemIcon = R.drawable.ic_billboard,
                        isSelected = selectedType == it.toString(),
                        onClick = {
                            selectedType = it.toString()
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
                                isSelected = selectedNumberOfSide == 1,
                                onClick = {
                                    selectedNumberOfSide = 1
                                },
                                modifier = Modifier.padding(end = 16.dp)
                            )
                            BillboardSideCountComp(
                                text = "2 sides",
                                isSelected = selectedNumberOfSide == 2,
                                onClick = {
                                    selectedNumberOfSide = 2
                                },
                                modifier = Modifier.padding(end = 16.dp)
                            )
                            BillboardSideCountComp(
                                text = "3 sides",
                                isSelected = selectedNumberOfSide == 3,
                                onClick = {
                                    selectedNumberOfSide = 3
                                },
                                modifier = Modifier.padding(end = 16.dp)
                            )
                            BillboardSideCountComp(
                                text = "4 sides",
                                isSelected = selectedNumberOfSide == 4,
                                onClick = {
                                    selectedNumberOfSide = 4
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

@Preview
@Composable
fun SelectCaptureTypeMainPreview() {
    BBScoutTheme {
        SelectCaptureTypeMain(null, CaptureRecordUiModel())
    }

}