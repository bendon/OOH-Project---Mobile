package com.edgetech.bbscout.features.capture.domain.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edgetech.bbscout.components.di.IoDispatcher
import com.edgetech.bbscout.components.di.MainDispatcher
import com.edgetech.bbscout.components.file_saver.FileSaver
import com.edgetech.bbscout.components.location.GetLocationInfo
import com.edgetech.bbscout.components.utils.logD
import com.edgetech.bbscout.components.utils.toJson
import com.edgetech.bbscout.components.utils.toLong
import com.edgetech.bbscout.data.data.local.dto.EntryRecord
import com.edgetech.bbscout.data.data.local.enities.BillboardDataEntity
import com.edgetech.bbscout.data.data.local.enities.EntryEntity
import com.edgetech.bbscout.data.data.local.enities.UserLocationEntity
import com.edgetech.bbscout.data.data.local.utils.LongList
import com.edgetech.bbscout.data.data.local.utils.StringList
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.FileResponse
import com.edgetech.bbscout.data.data.remote.gen_ai.data_model.bbscout.BBScoutAiAnalyserResponse
import com.edgetech.bbscout.data.data.remote.gen_ai.llm.FulltextAndImageInference
import com.edgetech.bbscout.data.repositories.MainRepository
import com.edgetech.bbscout.data.utils.BBScoutException
import com.edgetech.bbscout.data.utils.SimpleResource
import com.edgetech.bbscout.data.utils.onError
import com.edgetech.bbscout.data.utils.onSuccess
import com.edgetech.bbscout.features.capture.domain.model.BillboardExtractedInfo
import com.edgetech.bbscout.features.capture.domain.model.BillboardSides
import com.edgetech.bbscout.features.capture.domain.model.BillboardTypeErrorException
import com.edgetech.bbscout.features.capture.domain.model.BrandDescriptionErrorException
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordEventSink
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiEvent
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiModel
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiState
import com.edgetech.bbscout.features.capture.domain.model.NoBillboardFoundException
import com.edgetech.bbscout.features.capture.domain.model.closeUpDistance
import com.edgetech.bbscout.features.capture.domain.model.longShotDistance
import com.edgetech.bbscout.features.capture.domain.use_cases.isBillboardValid
import com.edgetech.bbscout.features.capture.presentation.capture_flow.create_new_record.NewCaptureDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.time.LocalDate
import javax.inject.Inject


@HiltViewModel
class CaptureRecordViewmodel @Inject constructor(
    private val repository: MainRepository,
    private val llmInference: FulltextAndImageInference,
    private val fileSaver: FileSaver,
    private val locationInfo: GetLocationInfo,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _captureUiState = MutableStateFlow(
        CaptureRecordUiState()
    )
    private val _captureUiEvent = MutableStateFlow<CaptureRecordUiEvent>(CaptureRecordUiEvent.Empty)


    val uiModel = CaptureRecordUiModel(
        captureUiState = _captureUiState,
        captureUiEvent = _captureUiEvent
    ) { eventSink ->
        when (eventSink) {
            is CaptureRecordEventSink.OnCaptureEvent -> {

                onCapture(eventSink)
            }

            is CaptureRecordEventSink.OnGetAllCaptures -> {
                onGetAllCaptures(eventSink)
            }

            is CaptureRecordEventSink.OnSaveCapture -> {
                onSaveCapture(eventSink)
            }

            is CaptureRecordEventSink.GetRecentCaptures -> {
                getRecentCaptures(eventSink)
            }

            CaptureRecordEventSink.ResetUiEvent -> {
                _captureUiEvent.update {
                    CaptureRecordUiEvent.Empty
                }
            }

            is CaptureRecordEventSink.OnGetCapture -> {
                getCapture(eventSink)
            }

            is CaptureRecordEventSink.OnSetLocation -> {

                onSetLocation(eventSink)


            }

            is CaptureRecordEventSink.OnEditCaptureEvent -> {
                onEditCapture(eventSink)
            }

            is CaptureRecordEventSink.OnAnalyseImage -> {
                //onAnalyseImage(eventSink)
            }

            is CaptureRecordEventSink.SetBillboardNumberOfSide -> {
                setBillboardNumberOfSide(eventSink)
            }

            is CaptureRecordEventSink.StartBillBoardSurvey -> {
                startBillBoardSurvey(eventSink)
            }

            is CaptureRecordEventSink.OnRetry -> {
                TODO()
            }

            CaptureRecordEventSink.OnBillboardDataSet -> {
                onBillboardSet(eventSink)
            }

            CaptureRecordEventSink.ResetCreatingCapture -> {
                resetCreatingCapture()
            }

            CaptureRecordEventSink.OnUiBack -> {
                onUiBack()
            }

            CaptureRecordEventSink.OnUiNext -> {
                onUiNext()
            }

            CaptureRecordEventSink.OnRecapture -> {
                onRecapture(eventSink)
            }

            is CaptureRecordEventSink.OnStartSideContentVerification -> {
                _captureUiState.update {
                    it.copy(
                        newCaptureSelectedSide = eventSink.side
                    )
                }
            }
        }
    }

    private fun onRecapture(eventSink: CaptureRecordEventSink) {
        val uiState = _captureUiState.value
        if (uiState.newCaptureSelectedScreen == NewCaptureDestinations.BillboardLongShot) {
            _captureUiState.update {
                it.copy(
                    billboardData = it.billboardData?.copy(
                        closedUpUri = null,
                        billboardImage = null
                    ),
                    sideOneExtractedInfo = it.sideOneExtractedInfo?.copy(
                        fileUri = null
                    )
                )
            }
        } else if (uiState.newCaptureSelectedScreen == NewCaptureDestinations.BillboardCloseUpShot) {
            val info = getBillboardSideToUpdate().copy(
                closedUpUri = null,
                billboardImage = null,
                objectType = null,
                billboardType = null
            )

            updateBillBoardState(info)

        }
    }

    private fun onSetLocation(eventSink: CaptureRecordEventSink.OnSetLocation) {
        val state = _captureUiState.value
        if (state.selectedLocation?.latitude != null && state.selectedLocation.longitude != null) {
            val distanceFromBillboard = calculateDistance(
                state.selectedLocation.latitude!!,
                state.selectedLocation.longitude!!,
                eventSink.location.latitude,
                eventSink.location.longitude
            )
            when (state.newCaptureSelectedScreen) {
                NewCaptureDestinations.BillboardCloseUpShot -> {
                    when (state.newCaptureSelectedSide) {
                        BillboardSides.SIDE_ONE -> {
                            _captureUiState.update {
                                it.copy(
                                    sideOneExtractedInfo = it.sideOneExtractedInfo?.copy(
                                        distanceFromBillboard = distanceFromBillboard.toDouble(),
                                        isDistanceValid = distanceFromBillboard.toDouble() in closeUpDistance
                                    )
                                )
                            }
                        }

                        BillboardSides.SIDE_TWO -> {
                            _captureUiState.update {
                                it.copy(
                                    sideTwoExtractedInfo = it.sideTwoExtractedInfo?.copy(
                                        distanceFromBillboard = distanceFromBillboard.toDouble(),
                                        isDistanceValid = distanceFromBillboard.toDouble() in closeUpDistance
                                    )
                                )
                            }
                        }

                        BillboardSides.SIDE_THREE -> {
                            _captureUiState.update {
                                it.copy(
                                    sideThreeExtractedInfo = it.sideThreeExtractedInfo?.copy(
                                        distanceFromBillboard = distanceFromBillboard.toDouble(),
                                        isDistanceValid = distanceFromBillboard.toDouble() in closeUpDistance
                                    )
                                )
                            }
                        }

                        BillboardSides.SIDE_FOUR -> {
                            _captureUiState.update {
                                it.copy(
                                    sideFourExtractedInfo = it.sideFourExtractedInfo?.copy(
                                        distanceFromBillboard = distanceFromBillboard.toDouble(),
                                        isDistanceValid = distanceFromBillboard.toDouble() in closeUpDistance
                                    )
                                )
                            }
                        }

                        BillboardSides.MAIN -> {}
                    }
                }

                NewCaptureDestinations.BillboardLongShot -> {
                    _captureUiState.update {
                        it.copy(
                            billboardData = it.billboardData?.copy(
                                distanceFromBillboard = distanceFromBillboard.toDouble(),
                                isDistanceValid = distanceFromBillboard.toDouble() in longShotDistance
                            )
                        )
                    }
                }

                else -> {}
            }
        }
        locationInfo.getLocationInfo(eventSink.location) { loc ->
            when (state.newCaptureSelectedScreen) {
                NewCaptureDestinations.BillboardCloseUpShot -> {
                    when (state.newCaptureSelectedSide) {
                        BillboardSides.SIDE_ONE -> {
                            _captureUiState.update {
                                it.copy(
                                    sideOneExtractedInfo = it.sideOneExtractedInfo?.copy(
                                        billboardLocation = loc
                                    )
                                )
                            }
                        }

                        BillboardSides.SIDE_TWO -> {
                            _captureUiState.update {
                                it.copy(
                                    sideTwoExtractedInfo = it.sideTwoExtractedInfo?.copy(
                                        billboardLocation = loc
                                    )
                                )
                            }
                        }

                        BillboardSides.SIDE_THREE -> {
                            _captureUiState.update {
                                it.copy(
                                    sideThreeExtractedInfo = it.sideThreeExtractedInfo?.copy(
                                        billboardLocation = loc
                                    )
                                )
                            }
                        }

                        BillboardSides.SIDE_FOUR -> {
                            _captureUiState.update {
                                it.copy(
                                    sideFourExtractedInfo = it.sideFourExtractedInfo?.copy(
                                        billboardLocation = loc
                                    )
                                )
                            }
                        }

                        else -> {}
                    }
                }

                NewCaptureDestinations.BillboardLocation -> {
                    _captureUiState.update {
                        it.copy(
                            selectedLocation = loc
                        )
                    }
                }

                NewCaptureDestinations.BillboardLongShot -> {
                    _captureUiState.update {
                        it.copy(
                            billboardData = it.billboardData?.copy(
                                billboardLocation = loc
                            )
                        )
                    }
                    logD("billboad  viewmodel is ${_captureUiState.value.billboardData} and loc is $loc")
                }

                else -> {

                }
            }
        }
        checkBillboardStatus()
    }

    private fun onUiNext() {
        val state = _captureUiState.value
        if (state.newCaptureSelectedScreen == NewCaptureDestinations.BillboardLocation) {
            _captureUiState.update {
                it.copy(
                    newCaptureSelectedScreen = NewCaptureDestinations.SelectBillboardType,
                    newCaptureCurrentStep = 2,
                    newCaptureBackEnabled = true,

                    )
            }
        }
        else if (state.newCaptureSelectedScreen == NewCaptureDestinations.SelectBillboardType) {
            _captureUiState.update {
                it.copy(
                    newCaptureSelectedScreen = NewCaptureDestinations.BillboardLongShot,
                    newCaptureCurrentStep = 3,
                )
            }
        }
        else if (state.newCaptureSelectedScreen == NewCaptureDestinations.BillboardLongShot) {
            _captureUiState.update {
                it.copy(
                    newCaptureSelectedScreen = NewCaptureDestinations.BillboardCloseUpShot,
                    newCaptureSelectedSide = BillboardSides.SIDE_ONE,
                    newCaptureCurrentStep = 4,
                )
            }
        }
        else if (state.newCaptureSelectedScreen == NewCaptureDestinations.BillboardCloseUpShot) {

            if (state.newCaptureSelectedSide == BillboardSides.SIDE_ONE) {
                if (state.createBillboardSideCount > 1) {
                    _captureUiState.update {
                        it.copy(
                            newCaptureSelectedScreen = NewCaptureDestinations.BillboardCloseUpShot,
                            newCaptureSelectedSide = BillboardSides.SIDE_TWO,
                            newCaptureCurrentStep = 5,
                        )
                    }

                } else
                    _captureUiState.update {
                        it.copy(
                            newCaptureSelectedScreen = NewCaptureDestinations.ConfirmAllDataCapture,
                            newCaptureCurrentStep = 5,
                        )
                    }
            } else if (state.newCaptureSelectedSide == BillboardSides.SIDE_TWO) {
                if (state.createBillboardSideCount > 2)
                    _captureUiState.update {
                        it.copy(
                            newCaptureSelectedScreen = NewCaptureDestinations.BillboardCloseUpShot,
                            newCaptureSelectedSide = BillboardSides.SIDE_THREE,
                            newCaptureCurrentStep = 6,
                        )
                    }
                else
                    _captureUiState.update {
                        it.copy(
                            newCaptureSelectedScreen = NewCaptureDestinations.ConfirmAllDataCapture,
                            newCaptureCurrentStep = 6,
                        )
                    }
            } else if (state.newCaptureSelectedSide == BillboardSides.SIDE_THREE) {
                if (state.createBillboardSideCount > 3)
                    _captureUiState.update {
                        it.copy(
                            newCaptureSelectedScreen = NewCaptureDestinations.BillboardCloseUpShot,
                            newCaptureSelectedSide = BillboardSides.SIDE_FOUR,
                            newCaptureCurrentStep = 7,
                        )
                    }
                else
                    _captureUiState.update {
                        it.copy(
                            newCaptureSelectedScreen = NewCaptureDestinations.ConfirmAllDataCapture,
                            newCaptureCurrentStep = 7,
                        )
                    }
            } else if (state.newCaptureSelectedSide == BillboardSides.SIDE_FOUR) {
                _captureUiState.update {
                    it.copy(
                        newCaptureSelectedScreen = NewCaptureDestinations.ConfirmAllDataCapture,
                        newCaptureCurrentStep = 8,
                    )
                }
            }
        }
        else if (state.newCaptureSelectedScreen == NewCaptureDestinations.ConfirmAllDataCapture) {
            _captureUiState.update {
                it.copy(
                    newCaptureSelectedScreen = NewCaptureDestinations.SubmitPhysicalData,
                    newCaptureCurrentStep = it.newCaptureCurrentStep + 1,
                )
            }
        }
        else if (state.newCaptureSelectedScreen == NewCaptureDestinations.SubmitPhysicalData) {
            _captureUiState.update {
                it.copy(
                    newCaptureSelectedScreen = NewCaptureDestinations.SelectBillboardToAddContentTo,
                    newCaptureCurrentStep = it.newCaptureCurrentStep + 1,
                    newCaptureNextIsEnabled = false
                )
            }
        }
        checkBillboardStatus()
    }

    private fun onUiBack() {
        val state = _captureUiState.value
        when (state.newCaptureSelectedScreen) {
            NewCaptureDestinations.BillboardCloseUpShot -> {
                when (state.newCaptureSelectedSide) {
                    BillboardSides.SIDE_ONE -> {
                        _captureUiState.update {
                            it.copy(
                                newCaptureSelectedScreen = NewCaptureDestinations.BillboardLongShot,
                                newCaptureCurrentStep = 3,
                            )
                        }
                    }

                    BillboardSides.SIDE_TWO -> {
                        _captureUiState.update {
                            it.copy(
                                newCaptureSelectedSide = BillboardSides.SIDE_ONE,
                                newCaptureCurrentStep = 4,
                            )
                        }
                    }

                    BillboardSides.SIDE_THREE -> {
                        _captureUiState.update {
                            it.copy(
                                newCaptureSelectedSide = BillboardSides.SIDE_TWO,
                                newCaptureCurrentStep = 5,
                            )
                        }
                    }

                    BillboardSides.SIDE_FOUR -> {
                        _captureUiState.update {
                            it.copy(
                                newCaptureSelectedSide = BillboardSides.SIDE_THREE,
                                newCaptureCurrentStep = 6,
                            )
                        }
                    }

                    BillboardSides.MAIN -> {
                        _captureUiState.update {
                            it.copy(
                                newCaptureSelectedScreen = NewCaptureDestinations.BillboardLocation,
                                newCaptureCurrentStep = 1,
                            )
                        }
                    }
                }
            }

            NewCaptureDestinations.BillboardLocation -> {

            }

            NewCaptureDestinations.BillboardLongShot -> {
                _captureUiState.update {
                    it.copy(
                        newCaptureSelectedScreen = NewCaptureDestinations.SelectBillboardType,
                        newCaptureCurrentStep = 2,
                    )
                }
            }

            NewCaptureDestinations.ConfirmAllDataCapture -> {
                _captureUiState.update {
                    it.copy(
                        newCaptureSelectedScreen = NewCaptureDestinations.BillboardCloseUpShot,
                        newCaptureSelectedSide = when (state.createBillboardSideCount) {
                            1 -> BillboardSides.SIDE_ONE
                            2 -> BillboardSides.SIDE_TWO
                            3 -> BillboardSides.SIDE_THREE
                            4 -> BillboardSides.SIDE_FOUR
                            else -> BillboardSides.MAIN
                        },
                        newCaptureCurrentStep = state.createBillboardSideCount + 3,
                    )
                }
            }

            NewCaptureDestinations.SelectBillboardType -> {
                _captureUiState.update {
                    it.copy(
                        newCaptureSelectedScreen = NewCaptureDestinations.BillboardLocation,
                        newCaptureCurrentStep = 1,
                        newCaptureBackEnabled = false,
                    )
                }
            }

            NewCaptureDestinations.SubmitPhysicalData -> {
                _captureUiState.update {
                    it.copy(
                        newCaptureSelectedScreen = NewCaptureDestinations.ConfirmAllDataCapture,
                        newCaptureCurrentStep = state.createBillboardSideCount + 4,
                        newCaptureNextText = "Next"
                    )
                }
            }

            NewCaptureDestinations.SelectBillboardToAddContentTo -> {
                _captureUiState.update {
                    it.copy(
                        newCaptureSelectedScreen = NewCaptureDestinations.SubmitPhysicalData,
                        newCaptureCurrentStep = state.createBillboardSideCount + 5,
                        newCaptureNextText = "Next"
                    )
                }
            }
        }
        checkBillboardStatus()
    }

    private fun resetCreatingCapture() {
        _captureUiState.update {
            CaptureRecordUiState().copy(
                userStat = it.userStat,
                selectedRecord = it.selectedRecord,
                recentCaptures = it.recentCaptures,
                allCaptures = it.allCaptures
            )
        }
    }

    private fun onBillboardSet(eventSink: CaptureRecordEventSink) {
        if (getBillboardSideToUpdate().billboardType.isNullOrEmpty()) {
            _captureUiEvent.update {
                CaptureRecordUiEvent.Error(BillboardTypeErrorException, eventSink)
            }
            return
        }
//        if (getBillboardSideToUpdate().brandCampaign.isNullOrEmpty()) {
//            _captureUiEvent.update {
//                CaptureRecordUiEvent.Error(BrandDescriptionErrorException, eventSink)
//            }
//            return
//        }
        _captureUiEvent.update {
            CaptureRecordUiEvent.BillboardDataSet
        }

    }

    private fun startBillBoardSurvey(eventSink: CaptureRecordEventSink.StartBillBoardSurvey) {
        _captureUiState.update {
            it.copy(
               // newCaptureSelectedSide = eventSink.billboard,
                newBillboardType = eventSink.type,
                createBillboardSideCount = eventSink.billboard.code,
                newCaptureNumberOfSteps = eventSink.billboard.code + 6 ,
                billboardData = BillboardExtractedInfo(billboardSideInfo = BillboardSides.MAIN)
            )
        }

        if (_captureUiState.value.sideOneExtractedInfo?.closedUpUri == null) {
            _captureUiState.update {
                it.copy(
                    sideOneExtractedInfo = BillboardExtractedInfo(
                        billboardSideInfo = BillboardSides.SIDE_ONE,
                        billboardType = eventSink.type
                    ),

                    )
            }

        }



        if (_captureUiState.value.sideTwoExtractedInfo?.closedUpUri == null) {
            _captureUiState.update {
                it.copy(
                    sideTwoExtractedInfo = BillboardExtractedInfo(
                        billboardSideInfo = BillboardSides.SIDE_TWO,
                        billboardType = eventSink.type
                    )
                )
            }

        }



        if (_captureUiState.value.sideThreeExtractedInfo?.closedUpUri == null) {
            _captureUiState.update {
                it.copy(
                    sideThreeExtractedInfo = BillboardExtractedInfo(
                        billboardSideInfo = BillboardSides.SIDE_THREE,
                        billboardType = eventSink.type
                    )
                )
            }

        }



        if (_captureUiState.value.sideFourExtractedInfo?.closedUpUri == null) {
            _captureUiState.update {
                it.copy(
                    sideFourExtractedInfo = BillboardExtractedInfo(
                        billboardSideInfo = BillboardSides.SIDE_FOUR,
                        billboardType = eventSink.type
                    )
                )
            }

        }



        if (_captureUiState.value.billboardData?.fileUri == null) {
            _captureUiState.update {
                it.copy(
                    billboardData = BillboardExtractedInfo(billboardSideInfo = eventSink.billboard)
                )
            }

        }

        logD("billboard survey started")
        checkBillboardStatus()
    }

    private fun setBillboardNumberOfSide(eventSink: CaptureRecordEventSink.SetBillboardNumberOfSide) {
        _captureUiState.update {
            it.copy(
                createBillboardSideCount = eventSink.sides
            )
        }
        _captureUiEvent.update {
            CaptureRecordUiEvent.BillboardNumberOfSideSet
        }
    }

    private fun getCapture(eventSink: CaptureRecordEventSink.OnGetCapture) {
        viewModelScope.launch(ioDispatcher) {
            _captureUiState.update {
                it.copy(isLoading = true)
            }
            var mainFilePath: String? = null
            var billboardFilePath: String? = null
            repository.getCaptureRecord(eventSink.captureId).onSuccess { data ->
                mainFilePath = data?.entryEntity?.mainFileUri
                billboardFilePath = data?.entryEntity?.billboardFileUri
                _captureUiState.update {
                    it.copy(
                        selectedRecord = data
                    )
                }
            }.onError { ex ->
                _captureUiEvent.update {
                    CaptureRecordUiEvent.Error(ex ?: BBScoutException("Unknown Error"), eventSink)
                }
            }
            //           listOf(
//                launch {
//                    if (!mainFilePath.isNullOrEmpty()) {
//                        fileSaver.getBitmapFromPath(mainFilePath!!).onSuccess { bit ->
//                            _captureUiState.update {
//                                it.copy(
//                                    selectedRecordMainImage = bit
//                                )
//                            }
//                        }
//                    }
//                },
//                launch {
//                    if (!billboardFilePath.isNullOrEmpty()) {
//                        fileSaver.getBitmapFromPath(billboardFilePath!!).onSuccess { bit ->
//                            _captureUiState.update {
//                                it.copy(
//                                    selectedRecordBillboardImage = bit
//                                )
//                            }
//                        }
//                    }
//
//                }).joinAll()
            _captureUiState.update {
                it.copy(isLoading = false)
            }
        }
    }


    private fun getRecentCaptures(eventSink: CaptureRecordEventSink.GetRecentCaptures) {
        viewModelScope.launch(ioDispatcher) {
            _captureUiState.update {
                it.copy(isLoading = true)
            }
            listOf(
                launch {
                    repository.getAllEntries().onSuccess { data ->
                        _captureUiState.update {
                            it.copy(
                                allCaptures = data ?: emptyList()
                            )
                        }
                    }.onError { ex ->
                        _captureUiEvent.update {
                            CaptureRecordUiEvent.Error(
                                ex ?: BBScoutException("Unknown Error"),
                                eventSink
                            )
                        }
                    }
                },
                launch {
                    repository.getMonthlyStats().onSuccess {
                        val res =
                            it //?.first { it.uploadMonth == LocalDateTime.now().monthValue &&  it.uploadYear == LocalDateTime.now().year}
                        _captureUiState.update {
                            it.copy(
                                userStat = res
                            )
                        }
                    }
                }
            ).joinAll()
            _captureUiState.update {
                it.copy(isLoading = false)
            }

        }
    }

    private fun onSaveCapture(eventSink: CaptureRecordEventSink.OnSaveCapture) {

        viewModelScope.launch(ioDispatcher) {

            _captureUiState.update {
                it.copy(isLoading = true)
            }
            val state = _captureUiState.value

            var boardCode: String? = null

            val side = state.sideOneExtractedInfo?.copy(
                fileUri = state.billboardData?.closedUpUri
            )
            saveBillboardInfo(side!!, {
                boardCode = it?.billboardData?.boardCode

            }) { ex ->
                _captureUiEvent.update {
                    CaptureRecordUiEvent.Error(ex ?: BBScoutException("Unknown Error"), eventSink)
                }
            }

            if (state.createBillboardSideCount == 1 && boardCode != null) {
                _captureUiEvent.update {
                    CaptureRecordUiEvent.CaptureRecordCreated(null)
                }
            }

            if (state.createBillboardSideCount > 1 && boardCode != null) {
                val allResults = mutableListOf<Any?>()
                listOf(
                    launch {
                        if (state.sideTwoExtractedInfo?.status == true) {
                            saveBillboardInfo(
                                state.sideTwoExtractedInfo.copy(
                                    parentBillboard = boardCode
                                ), {
                                    allResults.add(it)
                                }
                            ) {
                                allResults.add(it)
                            }
                        }

                    },
                    launch {
                        if (state.sideThreeExtractedInfo?.status == true) {
                            saveBillboardInfo(
                                state.sideThreeExtractedInfo.copy(
                                    parentBillboard = boardCode
                                ), {
                                    allResults.add(it)
                                }
                            ) {
                                allResults.add(it)
                            }
                        }
                    },
                    launch {
                        if (state.sideFourExtractedInfo?.status == true) {
                            saveBillboardInfo(
                                state.sideFourExtractedInfo.copy(
                                    parentBillboard = boardCode
                                ), {
                                    allResults.add(it)
                                }
                            ) {
                                allResults.add(it)
                            }
                        }
                    }
                ).joinAll()

                if (allResults.all { it is EntryRecord }) {
                    _captureUiEvent.update {
                        CaptureRecordUiEvent.CaptureRecordCreated(null)
                    }
                }
            }

            _captureUiState.update {
                it.copy(isLoading = false)
            }
        }
    }

    private suspend fun saveBillboardInfo(
        billboard: BillboardExtractedInfo,
        onResultSuccessful: (EntryRecord?) -> Unit,
        onResultFailed: (BBScoutException) -> Unit,
    ) {

        var billboardData = billboard
        var closedUpImageId: String? = null
        var envImage: String? = null

        coroutineScope {
            listOf(
                launch {
                    val captureClosedUp = billboard.closedUpUri
                    if (!captureClosedUp.isNullOrEmpty()) {
                        uploadImage(File(captureClosedUp)).onSuccess {
                            closedUpImageId = it?.id
                        }.onError { ex ->
                            _captureUiEvent.update {
                                CaptureRecordUiEvent.Error(
                                    ex ?: BBScoutException(),
                                    CaptureRecordEventSink.OnRetry(billboard)
                                )
                            }
                            onResultFailed(ex ?: BBScoutException())
                            return@launch
                        }
                    }
                },
                launch {
                    if (billboard.billboardSideInfo == BillboardSides.SIDE_ONE && !billboard.fileUri.isNullOrEmpty()) {
                        uploadImage(File(billboard.fileUri)).onSuccess {
                            envImage = it?.id
                        }.onError { ex ->

                        }
                    }

                }
            )
        }

        if (closedUpImageId != null) {
            billboardData = billboardData.copy(
                remoteCloseUpId = closedUpImageId,
                remoteFileId = envImage
            )
            val entry = entityRecordFromBillboardExtractedInfo(
                billboardData,
                _captureUiState.value.selectedLocation!!
            )
            repository.addEntryRecord(entry).onSuccess {
                onResultSuccessful(it)
            }.onError { ex ->
                onResultFailed(ex ?: BBScoutException())
                _captureUiEvent.update {
                    CaptureRecordUiEvent.Error(
                        ex ?: BBScoutException("Unknown Error"),
                        CaptureRecordEventSink.OnRetry(billboard)
                    )
                }
            }
        }
    }

    private suspend fun uploadImage(file: File): SimpleResource<FileResponse> {
        val multipartBody = buildMutipartBody(file)
        return repository.uploadFile(multipartBody)
    }

    private fun buildMutipartBody(file: File): MultipartBody {
        return MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.path.substringAfterLast("/"), file.asRequestBody())
            .build()
    }


    private fun onGetAllCaptures(eventSink: CaptureRecordEventSink.OnGetAllCaptures) {
        viewModelScope.launch(ioDispatcher) {
            _captureUiState.update {
                it.copy(isLoading = true)
            }
            repository.getAllEntries().onSuccess { data ->
                _captureUiState.update {
                    it.copy(
                        allCaptures = data?.sortedByDescending { it.entryEntity.createdAt }
                            ?: emptyList()
                    )
                }
            }.onError { ex ->
                _captureUiEvent.update {
                    CaptureRecordUiEvent.Error(ex ?: BBScoutException("Unknown Error"), eventSink)
                }
            }
            _captureUiState.update {
                it.copy(isLoading = false)
            }
        }
    }

    private fun onEditCapture(eventSink: CaptureRecordEventSink.OnEditCaptureEvent) {

        updateBillBoardState(eventSink.billboardData)

    }


    private fun onCapture(eventSink: CaptureRecordEventSink.OnCaptureEvent) {

        viewModelScope.launch(ioDispatcher) {

            val fileBitMap = fileSaver.getBitmapFromPath(eventSink.fileUri)

            if (fileBitMap.data != null) {

                val uiState = _captureUiState.value
                if (uiState.newCaptureSelectedScreen == NewCaptureDestinations.BillboardLongShot) {
                    _captureUiState.update {
                        it.copy(
                            billboardData = it.billboardData?.copy(
                                billboardImage = fileBitMap.data!!,
                                closedUpUri = eventSink.fileUri
                            ),
                            sideOneExtractedInfo = it.sideOneExtractedInfo?.copy(
                                fileUri = eventSink.fileUri
                            )
                        )
                    }
                    checkBillboardStatus()
                } else {
                    val billboard = getBillboardSideToUpdate().copy(
                        billboardImage = fileBitMap.data!!,
                        closedUpUri = eventSink.fileUri
                    )
                    updateBillBoardState(billboard)
                    onAnalyseImage(CaptureRecordEventSink.OnAnalyseImage)

                }
                _captureUiEvent.update {
                    CaptureRecordUiEvent.CaptureAdded
                }
            }


        }

    }

    private suspend fun onAnalyseImage(eventSink: CaptureRecordEventSink.OnAnalyseImage) {
        // viewModelScope.launch(ioDispatcher) {
        val uiData = getBillboardSideToUpdate()
        if (uiData.closedUpUri != null) {
            val fileMultipart =
                buildMutipartBody(File(uiData.closedUpUri))
            _captureUiState.update {
                it.copy(
                    analysingLoading = true
                )
            }
            repository.analyzeFile(fileMultipart)
                .onSuccess { res ->

//                    if (res?.object_type == null || res.billboard_type == null) {
//                        _captureUiEvent.update {
//                            CaptureRecordUiEvent.Error(
//                                NoBillboardFoundException, eventSink
//                            )
//                        }
//                        val bill = getBillboardSideToUpdate()
//                        updateBillBoardState(bill.copy(closedUpUri = null, billboardImage = null))
//
//                    }

                    val billboard = getBillboardSideToUpdate()
                    val updatedBillboard = updateBillboardSideAiData(billboard, res)
                    updateBillBoardState(updatedBillboard)

                }.onError {

                }
            _captureUiState.update {
                it.copy(
                    analysingLoading = false
                )
            }


        }
        //}
    }

    fun getBillboardSideToUpdate(): BillboardExtractedInfo {
        val uiData = _captureUiState.value
        var billBoardToUpdate: BillboardExtractedInfo? = null
        val sideBillboard = when (uiData.newCaptureSelectedSide) {
            BillboardSides.SIDE_ONE -> {
                billBoardToUpdate = uiData.sideOneExtractedInfo
            }

            BillboardSides.SIDE_TWO -> {
                billBoardToUpdate = uiData.sideTwoExtractedInfo
            }

            BillboardSides.SIDE_THREE -> {
                billBoardToUpdate = uiData.sideThreeExtractedInfo
            }

            BillboardSides.SIDE_FOUR -> {
                billBoardToUpdate = uiData.sideFourExtractedInfo
            }

            BillboardSides.MAIN -> {
                billBoardToUpdate = uiData.billboardData
            }
        }
        if (billBoardToUpdate == null) {
            throw (NoBillboardFoundException)
        }
        return billBoardToUpdate
    }

    fun updateBillboardSideAiData(
        existingData: BillboardExtractedInfo,
        res: BBScoutAiAnalyserResponse?
    ): BillboardExtractedInfo {
        var billboard = existingData.copy(
            brandName = res?.campaign_brand,
            brandSlogan = res?.campaign_description,
            brandCampaign = res?.campaign_description,
            objectType = res?.object_type,
            billboardWidth = res?.billboard_measurements?.width?.toString()
                ?: "",
            billboardLength = res?.billboard_measurements?.height?.toString()
                ?: "",
            phone = res?.contact_information?.phone,
            email = res?.contact_information?.email,
            siteUrl = res?.site_url,
            targetAge = res?.target_age,
            targetGender = res?.target_gender,
            campainSocials = res?.site_url,
           // billboardType = res?.billboard_type,
            ownerContacts = res?.owner?.owner_phone,
            ownerEmail = res?.owner?.owner_email,
            billboardOwner = res?.owner?.owner_name,
            structure = res?.structure,
            material = res?.material,
            angle = res?.angle,
            visibility = res?.visibility,
            illumination = res?.illumination,
            ownerWebsite = res?.owner?.owner_website
        )
      checkBillboardStatus()
        return billboard

    }

    private fun updateBillBoardState(billboardExtractedInfo: BillboardExtractedInfo) {
        when (_captureUiState.value.newCaptureSelectedSide) {
            BillboardSides.SIDE_ONE -> {
                _captureUiState.update {
                    it.copy(
                        sideOneExtractedInfo = billboardExtractedInfo
                    )
                }
            }

            BillboardSides.SIDE_TWO -> {
                _captureUiState.update {
                    it.copy(
                        sideTwoExtractedInfo = billboardExtractedInfo
                    )
                }
            }

            BillboardSides.SIDE_THREE -> {
                _captureUiState.update {
                    it.copy(
                        sideThreeExtractedInfo = billboardExtractedInfo
                    )
                }
            }

            BillboardSides.SIDE_FOUR -> {
                _captureUiState.update {
                    it.copy(
                        sideFourExtractedInfo = billboardExtractedInfo
                    )
                }
            }

            BillboardSides.MAIN -> {
                _captureUiState.update {
                    it.copy(
                        billboardData = billboardExtractedInfo
                    )
                }
            }
        }
        checkBillboardStatus()
    }

    private fun checkBillboardStatus() {
        val state = _captureUiState.value
        when (state.newCaptureSelectedScreen) {
            NewCaptureDestinations.BillboardCloseUpShot -> {
                when (state.newCaptureSelectedSide) {
                    BillboardSides.SIDE_ONE -> {
                        val valid = isBillboardValid(state.sideOneExtractedInfo)
                        _captureUiState.update {
                            it.copy(
                                newCaptureNextIsEnabled = valid,
                                sideOneExtractedInfo = it.sideOneExtractedInfo?.copy(
                                    status = valid
                                )
                            )
                        }
                    }

                    BillboardSides.SIDE_TWO -> {
                        val valid = isBillboardValid(state.sideTwoExtractedInfo)
                        _captureUiState.update {
                            it.copy(
                                newCaptureNextIsEnabled = valid,
                                sideTwoExtractedInfo = it.sideTwoExtractedInfo?.copy(
                                    status = valid
                                )
                            )
                        }

                    }

                    BillboardSides.SIDE_THREE -> {
                        val valid = isBillboardValid(state.sideThreeExtractedInfo)
                        _captureUiState.update {
                            it.copy(
                                newCaptureNextIsEnabled = valid,
                                sideThreeExtractedInfo = it.sideThreeExtractedInfo?.copy(
                                    status = valid
                                )
                            )
                        }
                    }

                    BillboardSides.SIDE_FOUR -> {
                        val valid = isBillboardValid(state.sideFourExtractedInfo)
                        _captureUiState.update {
                            it.copy(
                                newCaptureNextIsEnabled = valid,
                                sideFourExtractedInfo = it.sideFourExtractedInfo?.copy(
                                    status = valid
                                )
                            )
                        }
                    }

                    BillboardSides.MAIN -> {

                    }
                }

            }

            NewCaptureDestinations.BillboardLocation -> {
                _captureUiState.update {
                    it.copy(
                        newCaptureNextIsEnabled = state.selectedLocation != null,
                        newCaptureBackEnabled = false,
                    )
                }
            }

            NewCaptureDestinations.BillboardLongShot -> {
                _captureUiState.update {
                    it.copy(
                        newCaptureNextIsEnabled = it.billboardData?.isDistanceValid == true && it.billboardData.billboardLocation != null && it.billboardData.closedUpUri != null
                    )
                }
            }

            NewCaptureDestinations.ConfirmAllDataCapture -> {
                _captureUiState.update {
                    it.copy(
                        newCaptureNextIsEnabled = true
                    )
                }
            }

            NewCaptureDestinations.SelectBillboardType -> {
                _captureUiState.update {
                    it.copy(
                        newCaptureNextIsEnabled = !it.newBillboardType.isNullOrEmpty() && it.createBillboardSideCount != 0
                    )
                }
            }

            NewCaptureDestinations.SubmitPhysicalData -> {
                _captureUiState.update {
                    it.copy(
                        newCaptureNextIsEnabled = true
                    )
                }
            }

            NewCaptureDestinations.SelectBillboardToAddContentTo -> {
                _captureUiState.update {
                    it.copy(
                        //newCaptureNextIsEnabled = true,
                        newCaptureNextText = "Submit"
                    )
                }
            }
        }
    }

    fun entityRecordFromBillboardExtractedInfo(
        info: BillboardExtractedInfo,
        userLocationEntity: UserLocationEntity
    ): EntryRecord {
        return EntryRecord(
            entryEntity = EntryEntity(
                brand = info.brandName,
                rawText = info.brandCampaign,
                createdAt = LocalDate.now().toLong(),
                updatedAt = LocalDate.now().toLong(),
                phone = LongList.fromList(info.phone)?.toJson(),
                email = StringList.fromList(info.email)?.toJson(),
                siteUrl = StringList.fromList(info.siteUrl)?.toJson(),
                campainSocials = StringList.fromList(info.campainSocials)?.toJson(),
                products = StringList.fromList(info.products)?.toJson(),
                targetGender = info.targetGender,
                targetAge = info.targetAge,
            ),
            billboardData = BillboardDataEntity(
                height = info.billboardLength?.toDoubleOrNull(),
                width = info.billboardWidth?.toDoubleOrNull(),
                type = info.billboardType,
                owner = info.billboardOwner,
                objectType = info.objectType,
                city = userLocationEntity.locationCity,
                ownerContacts = LongList.fromList(info.ownerContacts)?.toJson(),
                ownerEmail = StringList.fromList(info.ownerEmail)?.toJson(),
                structure = info.structure,
                material = info.material,
                occupied = info.isOccupied,
                angle = info.angle,
                unitOfMeasurement = info.unitOfMeasurement,
                imageId = info.remoteFileId,
                closeUpImageId = info.remoteCloseUpId,
                visibility = info.visibility,
                illumination = info.illumination,
                parentBoardCode = info.parentBillboard
            ),
            otherData = emptyList(),
            location = userLocationEntity
        )
    }

    fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0] // Distance in meters
    }

}