package com.edgetech.bbscout.features.capture.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edgetech.bbscout.components.di.IoDispatcher
import com.edgetech.bbscout.components.di.MainDispatcher
import com.edgetech.bbscout.components.file_saver.FileSaver
import com.edgetech.bbscout.components.location.GetLocationInfo
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

                locationInfo.getLocationInfo(eventSink.location) { loc ->
                    if (_captureUiState.value.selectedBillboardSidesType != BillboardSides.MAIN) {
                        _captureUiState.update {
                            it.copy(
                                selectedLocation = loc
                            )
                        }
                    } else {
                        _captureUiState.update {
                            it.copy(
                                billboardData = _captureUiState.value.billboardData?.copy(
                                    billboardLocation = loc
                                )
                            )
                        }
                    }
                }

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
        }
    }

    private fun resetCreatingCapture() {
        _captureUiState.update {
            it.copy(
                selectedRecord = null,
                selectedRecordMainImage = null,
                selectedRecordBillboardImage = null,
                selectedBillboardSidesType = BillboardSides.MAIN,
                sideOneExtractedInfo = null,
                sideTwoExtractedInfo = null,
                sideThreeExtractedInfo = null,
                sideFourExtractedInfo = null,
                billboardData = null,
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
        if (getBillboardSideToUpdate().brandCampaign.isNullOrEmpty()) {
            _captureUiEvent.update {
                CaptureRecordUiEvent.Error(BrandDescriptionErrorException, eventSink)
            }
            return
        }
        _captureUiEvent.update {
            CaptureRecordUiEvent.BillboardDataSet
        }

    }

    private fun startBillBoardSurvey(eventSink: CaptureRecordEventSink.StartBillBoardSurvey) {
        _captureUiState.update {
            it.copy(
                selectedBillboardSidesType = eventSink.billboard,

                )
        }
        when (eventSink.billboard) {
            BillboardSides.SIDE_ONE -> {
                if (_captureUiState.value.sideOneExtractedInfo?.closedUpUri == null) {
                    _captureUiState.update {
                        it.copy(
                            sideOneExtractedInfo = BillboardExtractedInfo(billboardSideInfo = eventSink.billboard)
                        )
                    }
                    _captureUiEvent.update {
                        CaptureRecordUiEvent.StartBillBoardSurvey(eventSink.billboard)
                    }
                } else {
                    _captureUiEvent.update {
                        CaptureRecordUiEvent.ContinueBillBoardSurvey(eventSink.billboard)
                    }
                }


            }

            BillboardSides.SIDE_TWO -> {
                if (_captureUiState.value.sideTwoExtractedInfo?.closedUpUri == null) {
                    _captureUiState.update {
                        it.copy(
                            sideTwoExtractedInfo = BillboardExtractedInfo(billboardSideInfo = eventSink.billboard)
                        )
                    }
                    _captureUiEvent.update {
                        CaptureRecordUiEvent.StartBillBoardSurvey(eventSink.billboard)
                    }
                } else {
                    _captureUiEvent.update {
                        CaptureRecordUiEvent.ContinueBillBoardSurvey(eventSink.billboard)
                    }
                }
            }

            BillboardSides.SIDE_THREE -> {
                if (_captureUiState.value.sideThreeExtractedInfo?.closedUpUri == null) {
                    _captureUiState.update {
                        it.copy(
                            sideThreeExtractedInfo = BillboardExtractedInfo(billboardSideInfo = eventSink.billboard)
                        )
                    }
                    _captureUiEvent.update {
                        CaptureRecordUiEvent.StartBillBoardSurvey(eventSink.billboard)
                    }
                } else {
                    _captureUiEvent.update {
                        CaptureRecordUiEvent.ContinueBillBoardSurvey(eventSink.billboard)
                    }
                }
            }

            BillboardSides.SIDE_FOUR -> {
                if (_captureUiState.value.sideFourExtractedInfo?.closedUpUri == null) {
                    _captureUiState.update {
                        it.copy(
                            sideFourExtractedInfo = BillboardExtractedInfo(billboardSideInfo = eventSink.billboard)
                        )
                    }
                    _captureUiEvent.update {
                        CaptureRecordUiEvent.StartBillBoardSurvey(eventSink.billboard)
                    }
                } else {
                    _captureUiEvent.update {
                        CaptureRecordUiEvent.ContinueBillBoardSurvey(eventSink.billboard)
                    }
                }
            }

            BillboardSides.MAIN -> {
                if (_captureUiState.value.billboardData?.fileUri == null) {
                    _captureUiState.update {
                        it.copy(
                            billboardData = BillboardExtractedInfo(billboardSideInfo = eventSink.billboard)
                        )
                    }
                    _captureUiEvent.update {
                        CaptureRecordUiEvent.StartBillBoardSurvey(eventSink.billboard)
                    }
                } else {
                    _captureUiEvent.update {
                        CaptureRecordUiEvent.ContinueBillBoardSurvey(eventSink.billboard)
                    }
                }
            }
        }

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

            if (state.createBillboardSideCount == 1 && boardCode != null){
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

//        val billboard = getBillboardSideToUpdate().copy(
//            fullImage = fileBitMap.data!!,
//            fileUri = eventSink.fileUri
//        )
        updateBillBoardState(eventSink.billboardData)

//        _captureUiState.update {
//            it.copy(
//                billboardData = eventSink.billboardData
//            )
//        }
//        _captureUiEvent.update {
//            CaptureRecordUiEvent.CaptureAdded
//        }
    }


    private fun onCapture(eventSink: CaptureRecordEventSink.OnCaptureEvent) {

        viewModelScope.launch(ioDispatcher) {

            val fileBitMap = fileSaver.getBitmapFromPath(eventSink.fileUri)

            if (fileBitMap.data != null) {


                val billboard = getBillboardSideToUpdate().copy(
                    billboardImage = fileBitMap.data!!,
                    closedUpUri = eventSink.fileUri
                )
                updateBillBoardState(billboard)

                _captureUiEvent.update {
                    CaptureRecordUiEvent.CaptureAdded
                }
                if (billboard.billboardSideInfo != BillboardSides.MAIN) {
                    onAnalyseImage(CaptureRecordEventSink.OnAnalyseImage)
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

                    if (res?.object_type == null || res?.billboard_type == null) {
                        _captureUiEvent.update {
                            CaptureRecordUiEvent.Error(
                                NoBillboardFoundException, eventSink
                            )
                        }
                       val bill = getBillboardSideToUpdate()
                        updateBillBoardState(bill.copy(closedUpUri = null, billboardImage = null))

                    }

                    val billboard = getBillboardSideToUpdate()
                    val updatedBillboard = updateBillboardSideAiData(billboard, res)
                    updateBillBoardState(updatedBillboard)

                }.onError {
                    println("Error...${it}")

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
        val sideBillboard = when (uiData.selectedBillboardSidesType) {
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
            billboardType = res?.billboard_type,
        )
        if (billboard.billboardSideInfo != BillboardSides.MAIN && !billboard.billboardType.isNullOrEmpty()) {
            billboard = billboard.copy(
                status = true
            )

        }
        return billboard

    }

    fun updateBillBoardState(billboardExtractedInfo: BillboardExtractedInfo) {
        when (_captureUiState.value.selectedBillboardSidesType) {
            BillboardSides.SIDE_ONE -> {
                _captureUiState.update {
                    it.copy(
                        sideOneExtractedInfo = billboardExtractedInfo.copy(
                            status = !billboardExtractedInfo.billboardType.isNullOrEmpty()
                        )
                    )
                }
            }

            BillboardSides.SIDE_TWO -> {
                _captureUiState.update {
                    it.copy(
                        sideTwoExtractedInfo = billboardExtractedInfo.copy(
                            status = !billboardExtractedInfo.billboardType.isNullOrEmpty()
                        )
                    )
                }
            }

            BillboardSides.SIDE_THREE -> {
                _captureUiState.update {
                    it.copy(
                        sideThreeExtractedInfo = billboardExtractedInfo.copy(
                            status = !billboardExtractedInfo.billboardType.isNullOrEmpty()
                        )
                    )
                }
            }

            BillboardSides.SIDE_FOUR -> {
                _captureUiState.update {
                    it.copy(
                        sideFourExtractedInfo = billboardExtractedInfo.copy(
                            status = !billboardExtractedInfo.billboardType.isNullOrEmpty()
                        )
                    )
                }
            }

            BillboardSides.MAIN -> {
                _captureUiState.update {
                    it.copy(
                        billboardData = billboardExtractedInfo.copy(
                            status = !billboardExtractedInfo.closedUpUri.isNullOrEmpty()
                        )
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
                ownerContacts = StringList.fromList(info.ownerContacts)?.toJson(),
                ownerEmail = StringList.fromList(info.ownerEmail)?.toJson(),
                structure = info.structure,
                material = info.material,
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

}