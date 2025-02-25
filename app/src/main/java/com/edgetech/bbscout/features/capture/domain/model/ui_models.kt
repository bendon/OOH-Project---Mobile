package com.edgetech.bbscout.features.capture.domain.model

import android.graphics.Bitmap
import com.edgetech.bbscout.components.domain_util.AppBasicUiEvent
import com.edgetech.bbscout.components.domain_util.AppEventSink
import com.edgetech.bbscout.data.data.local.dto.EntryRecord
import com.edgetech.bbscout.data.data.local.enities.BillboardDataEntity
import com.edgetech.bbscout.data.data.local.enities.UserLocationEntity
import com.edgetech.bbscout.data.utils.BBScoutException
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


data class CaptureRecordUiModel(
    val captureUiState: StateFlow<CaptureRecordUiState> = MutableStateFlow(CaptureRecordUiState()),
    val captureUiEvent: StateFlow<CaptureRecordUiEvent> = MutableStateFlow(CaptureRecordUiEvent.Empty),
    val captureEventSink: (CaptureRecordEventSink) -> Unit = {}

)


data class CaptureRecordUiState(
    val isLoading: Boolean = false,
    val allCaptures: List<EntryRecord> = emptyList(),
    val recentCaptures: List<EntryRecord> = emptyList(),
    val billboardData: BillboardExtractedInfo? = null,
    val selectedRecord: EntryRecord? = null,
    val selectedRecordMainImage: Bitmap? = null,
    val selectedRecordBillboardImage: Bitmap? = null,
    val selectedLocation: UserLocationEntity? = null,
)

sealed class CaptureRecordUiEvent {
    data class CaptureRecordCreated(val entryId: Long) : CaptureRecordUiEvent()

    data class Error(val exception: BBScoutException, val eventSink : AppEventSink) : CaptureRecordUiEvent()

    object Empty : CaptureRecordUiEvent()

    data object CaptureAdded : CaptureRecordUiEvent()

}


sealed class CaptureRecordEventSink : AppEventSink {

    data class OnCaptureEvent(
        val billboardData: BillboardExtractedInfo
    ) : CaptureRecordEventSink()


    data class OnSaveCapture(
        val mainFileUri: String? = null,
        val billboardFileUri: String? = null,
        val billboardData: BillboardDataEntity? = null,
        val location: UserLocationEntity? = null,

        val advertDescription: String? = null,
        val extractedEntity: String? = null,
        val qrCode: List<String>? = null,
        val entityInfos: List<EntityInfo>? = null,
        val billboardOwnerName: String? = null,
        val brandName: String? = null,
        val campaignDescription: String? = null,
    ) : CaptureRecordEventSink()

    data object OnGetAllCaptures : CaptureRecordEventSink()

    data object GetRecentCaptures : CaptureRecordEventSink()

    data object ResetState : CaptureRecordEventSink()

    data class OnGetCapture(val captureId: Long): CaptureRecordEventSink()

    data class OnSetLocation(val location: LatLng) : CaptureRecordEventSink()
}


