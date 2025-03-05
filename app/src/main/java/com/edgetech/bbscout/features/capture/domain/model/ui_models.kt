package com.edgetech.bbscout.features.capture.domain.model

import android.graphics.Bitmap
import com.edgetech.bbscout.components.domain_util.AppBasicUiEvent
import com.edgetech.bbscout.components.domain_util.AppEventSink
import com.edgetech.bbscout.data.data.local.dto.EntryRecord
import com.edgetech.bbscout.data.data.local.enities.BillboardDataEntity
import com.edgetech.bbscout.data.data.local.enities.UserLocationEntity
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.BBScoutUserStat
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.BBScoutUserStatResponse
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
    val analysingLoading: Boolean = false,
    val allCaptures: List<EntryRecord> = emptyList(),
    val recentCaptures: List<EntryRecord> = emptyList(),
    val createBillboardSideCount: Int = 0,
    /**
     * This field will carry the location from far
     * and the wide shot image
     */
    val billboardData: BillboardExtractedInfo? = null,
    val sideOneExtractedInfo: BillboardExtractedInfo? = null,
    val sideTwoExtractedInfo: BillboardExtractedInfo? = null,
    val sideThreeExtractedInfo: BillboardExtractedInfo? = null,
    val sideFourExtractedInfo: BillboardExtractedInfo? = null,
    val selectedRecord: EntryRecord? = null,
    val userStat: BBScoutUserStat? = null,
    val selectedRecordMainImage: Bitmap? = null,
    val selectedRecordBillboardImage: Bitmap? = null,
    /**
     * This field will carry the location from close
     */
    val selectedLocation: UserLocationEntity? = null,
    val selectedBillboardSidesType: BillboardSides = BillboardSides.MAIN,

)

sealed class CaptureRecordUiEvent {
    data class CaptureRecordCreated(val entry: EntryRecord?) : CaptureRecordUiEvent()

    data class Error(val exception: BBScoutException, val eventSink : AppEventSink) : CaptureRecordUiEvent()

    object Empty : CaptureRecordUiEvent()

    data class StartBillBoardSurvey(val billboard: BillboardSides): CaptureRecordUiEvent()

    data class ContinueBillBoardSurvey(val billboard: BillboardSides): CaptureRecordUiEvent()

    data object BillboardDataSet: CaptureRecordUiEvent()

    data object BillboardNumberOfSideSet : CaptureRecordUiEvent()


    data object CaptureAdded : CaptureRecordUiEvent()

}


sealed class CaptureRecordEventSink : AppEventSink {

    data class SetBillboardNumberOfSide(val sides: Int) : CaptureRecordEventSink()


    data class StartBillBoardSurvey(val billboard: BillboardSides): CaptureRecordEventSink()


    data object OnBillboardDataSet : CaptureRecordEventSink()

    data class OnRetry(val billboardExtractedInfo: BillboardExtractedInfo) : CaptureRecordEventSink()


    data object OnAnalyseImage : CaptureRecordEventSink()

    data class OnCaptureEvent(
        val fileUri: String,
    ): CaptureRecordEventSink()

    data class OnEditCaptureEvent(
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

    data class OnGetCapture(val captureId: String): CaptureRecordEventSink()

    data class OnSetLocation(val location: LatLng) : CaptureRecordEventSink()
}


