package com.edgetech.bbscout.features.capture.domain.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edgetech.bbscout.components.di.DefaultDispatcher
import com.edgetech.bbscout.components.di.IoDispatcher
import com.edgetech.bbscout.components.di.MainDispatcher
import com.edgetech.bbscout.components.file_saver.FileSaver
import com.edgetech.bbscout.components.location.GetLocationInfo
import com.edgetech.bbscout.components.utils.toJson
import com.edgetech.bbscout.components.utils.toLong
import com.edgetech.bbscout.data.data.local.dto.EntryRecord
import com.edgetech.bbscout.data.data.local.enities.BillboardDataEntity
import com.edgetech.bbscout.data.data.local.enities.EntryEntity
import com.edgetech.bbscout.data.data.local.enities.OtherDataEntity
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
import com.edgetech.bbscout.features.capture.domain.model.LocationErrorException
import com.edgetech.bbscout.features.capture.domain.model.NoBillboardFoundException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
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

            CaptureRecordEventSink.ResetState -> {
                _captureUiEvent.update {
                    CaptureRecordUiEvent.Empty
                }
            }

            is CaptureRecordEventSink.OnGetCapture -> {
                getCapture(eventSink)
            }

            is CaptureRecordEventSink.OnSetLocation -> {
                locationInfo.getLocationInfo(eventSink.location) { loc ->
                    _captureUiState.update {
                        it.copy(
                            selectedLocation = loc
                        )
                    }
                }
            }

            is CaptureRecordEventSink.OnEditCaptureEvent -> {
                onEditCapture(eventSink)
            }

            is CaptureRecordEventSink.OnAnalyseImage -> {
                //onAnalyseImage(eventSink)
            }

            CaptureRecordEventSink.GetBillboardSides -> {
                getBillboardSides(eventSink)
            }
            CaptureRecordEventSink.OnBillboardInfoMove -> {
                onBillboardInfoMove(eventSink)
            }
            CaptureRecordEventSink.OnBillboardStructureMove -> {
                onBillboardStructureMove(eventSink)
            }
            CaptureRecordEventSink.OnCampaignSet -> {
                onCampaignSet(eventSink)
            }
            is CaptureRecordEventSink.SetBillboardNumberOfSide -> {
                setBillboardNumberOfSide(eventSink)
            }
            is CaptureRecordEventSink.StartBillBoardSurvey -> {
                startBillBoardSurvey(eventSink)
            }
        }
    }

    private fun startBillBoardSurvey(eventSink: CaptureRecordEventSink.StartBillBoardSurvey) {
        _captureUiState.update {
            it.copy(
                selectedBillboardSidesType = eventSink.billboard
            )
        }
        when(eventSink.billboard){
            BillboardSides.SIDE_ONE -> {
               if (_captureUiState.value.sideOneExtractedInfo == null){
                   _captureUiState.update {
                       it.copy(
                           sideOneExtractedInfo = BillboardExtractedInfo()
                       )
                   }
               }


            }
            BillboardSides.SIDE_TWO -> {
                if (_captureUiState.value.sideTwoExtractedInfo == null){
                    _captureUiState.update {
                        it.copy(
                            sideTwoExtractedInfo = BillboardExtractedInfo()
                        )
                    }
                }
            }
            BillboardSides.SIDE_THREE -> {
                if (_captureUiState.value.sideThreeExtractedInfo == null){
                    _captureUiState.update {
                        it.copy(
                            sideThreeExtractedInfo = BillboardExtractedInfo()
                        )
                    }
                }
            }
            BillboardSides.SIDE_FOUR -> {
                if (_captureUiState.value.sideFourExtractedInfo == null){
                    _captureUiState.update {
                        it.copy(
                            sideFourExtractedInfo = BillboardExtractedInfo()
                        )
                    }
                }
            }
            BillboardSides.MAIN -> {
                if (_captureUiState.value.billboardData == null){
                    _captureUiState.update {
                        it.copy(
                            billboardData = BillboardExtractedInfo()
                        )
                    }
                }
            }
        }
        _captureUiEvent.update {
            CaptureRecordUiEvent.StartBillBoardSurvey(eventSink.billboard)
        }
    }

    private fun setBillboardNumberOfSide(eventSink: CaptureRecordEventSink.SetBillboardNumberOfSide) {
        _captureUiState.update {
            it.copy(
                createBillboardSideCount = eventSink.sides
            )
        }
    }

    private fun onCampaignSet(eventSink: CaptureRecordEventSink) {
        if (getBillboardSideToUpdate().brandCampaign.isNullOrEmpty()) {
            _captureUiEvent.update {
                CaptureRecordUiEvent.Error(BrandDescriptionErrorException, eventSink)
            }
            return
        }
    }

    private fun onBillboardStructureMove(eventSink: CaptureRecordEventSink) {
        if (getBillboardSideToUpdate().billboardType.isNullOrEmpty()) {
            _captureUiEvent.update {
                CaptureRecordUiEvent.Error(BillboardTypeErrorException, eventSink)
            }
            return
        }
        _captureUiEvent.update {
            CaptureRecordUiEvent.BillboardInfoMove
        }
    }

    private fun onBillboardInfoMove(eventSink: CaptureRecordEventSink) {
        _captureUiEvent.update {
            CaptureRecordUiEvent.BillboardInfoMove
        }
    }

    private fun getBillboardSides(eventSink: CaptureRecordEventSink) {
        TODO("Not yet implemented")
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
            if (state.createBillboardSideCount == 1){
                val side = state.sideOneExtractedInfo?.copy(
                    fileUri = state.billboardData?.fileUri
                )
                saveBillboardInfo(side!!){
                    if (it)
                    _captureUiEvent.update {
                        CaptureRecordUiEvent.CaptureRecordCreated(null)
                    }
                }
            } else if (state.createBillboardSideCount > 1){
                val dummyBillboard = repository.addEntryRecord(EntryRecord(EntryEntity(), emptyList(), null, null)).onSuccess {

                }.onError { ex ->
                    _captureUiEvent.update {
                        CaptureRecordUiEvent.Error(ex ?: BBScoutException(), eventSink)
                    }
                }
                val allResults = mutableListOf<Boolean>()
                if (dummyBillboard.data?.billboardData?.remoteId != null) {
                    val dummyBillboardId = dummyBillboard.data?.billboardData?.remoteId
                    listOf(
                        launch {
                            if (state.sideOneExtractedInfo != null) {
                                saveBillboardInfo(
                                    state.sideOneExtractedInfo.copy(
                                        parentBillboard = dummyBillboardId
                                    )
                                ) {
                                    allResults.add(it)
                                }
                            }

                        },
                        launch {
                            if (state.sideTwoExtractedInfo != null) {
                                saveBillboardInfo(
                                    state.sideTwoExtractedInfo.copy(
                                        parentBillboard = dummyBillboardId
                                    )
                                ) {
                                    allResults.add(it)
                                }
                            }

                        },
                        launch {
                            if (state.sideThreeExtractedInfo != null) {
                                saveBillboardInfo(
                                    state.sideThreeExtractedInfo.copy(
                                        parentBillboard = dummyBillboardId
                                    )
                                ) {
                                    allResults.add(it)
                                }
                            }
                        },
                        launch {
                            if (state.sideFourExtractedInfo != null) {
                                saveBillboardInfo(
                                    state.sideFourExtractedInfo.copy(
                                        parentBillboard = dummyBillboardId
                                    )
                                ) {
                                    allResults.add(it)
                                }
                            }
                        }
                    ).joinAll()
                }
                if (allResults.all { it }) {
                    _captureUiEvent.update {
                        CaptureRecordUiEvent.CaptureRecordCreated(null)
                    }
                }
            }




//            val newEntry = EntryRecord(
//                entryEntity = EntryEntity(
//                    brand = _captureUiState.value.billboardData?.brandName,
//                    rawText = _captureUiState.value.billboardData?.brandCampaign
//                        ?: _captureUiState.value.billboardData?.rawText,
//                    //mainFileUri = mainFile?.path,
//                    //billboardFileUri = billboardFile?.path,
//                    remoteFileId = fullImageId,
//                    augmentedText = _captureUiState.value.billboardData?.brandCampaign,
//                    createdAt = LocalDate.now().toLong(),
//                    updatedAt = LocalDate.now().toLong(),
//                    phone = LongList.fromList(_captureUiState.value.billboardData?.phone)?.toJson(),
//                    email = StringList.fromList(_captureUiState.value.billboardData?.email)
//                        ?.toJson(),
//                    siteUrl = StringList.fromList(_captureUiState.value.billboardData?.siteUrl)
//                        ?.toJson(),
//                    campainSocials = StringList.fromList(_captureUiState.value.billboardData?.campainSocials)
//                        ?.toJson(),
//                    products = StringList.fromList(_captureUiState.value.billboardData?.products)
//                        ?.toJson(),
//                    targetGender = _captureUiState.value.billboardData?.targetGender,
//                    targetAge = _captureUiState.value.billboardData?.targetAge,
//                ),
//                otherData = (eventSink.qrCode?.map { OtherDataEntity(type = "QrCode", value = it) }
//                    ?: emptyList()) +
//                        (eventSink.entityInfos?.map {
//                            OtherDataEntity(
//                                type = it.type,
//                                value = it.text
//                            )
//                        }
//                            ?: emptyList()),
//                location = _captureUiState.value.selectedLocation,
//                billboardData = BillboardDataEntity(
//                    height = _captureUiState.value.billboardData?.billboardLength?.toDoubleOrNull(),
//                    width = _captureUiState.value.billboardData?.billboardWidth?.toDoubleOrNull(),
//                    type = _captureUiState.value.billboardData?.billboardType,
//                    owner = _captureUiState.value.billboardData?.billboardOwner,
//                    objectType = _captureUiState.value.billboardData?.objectType,
//
//                    )
//            )
//            repository.addEntryRecord(newEntry).onSuccess { result ->
//                _captureUiEvent.update {
//                    CaptureRecordUiEvent.CaptureRecordCreated(result)
//                }
//                _captureUiState.update {
//                    it.copy(
//                        selectedLocation = null,
//                        billboardData = null
//                    )
//                }
//            }.onError { ex ->
//                _captureUiEvent.update {
//                    CaptureRecordUiEvent.Error(ex ?: BBScoutException("Unknown Error"), eventSink)
//                }
//            }
            _captureUiState.update {
                it.copy(isLoading = false)
            }
        }
    }

    private suspend fun saveBillboardInfo(billboard: BillboardExtractedInfo, onResultSuccessful: (Boolean) -> Unit){

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
                                CaptureRecordUiEvent.Error(ex ?: BBScoutException(), CaptureRecordEventSink.OnRetry(billboard))
                            }
                            onResultSuccessful(false)
                            return@launch
                        }
                    }
                },
                launch {
                    if (billboard.billboardSideInfo == BillboardSides.SIDE_ONE && !billboard.fileUri.isNullOrEmpty()){
                        uploadImage(File(billboard.fileUri)).onSuccess {
                            envImage = it?.id
                        }.onError { ex ->

                        }
                    }

                }
            )
        }

        if (closedUpImageId != null) {
            billboardData =  billboardData.copy(
                closedUpUri = closedUpImageId,
                fileUri = envImage
            )
            val entry = entityRecordFromBillboardExtractedInfo(
                billboardData,
                _captureUiState.value.selectedLocation!!
            )
            repository.addEntryRecord(entry).onSuccess {
                onResultSuccessful(true)
            }.onError { ex ->
                onResultSuccessful(false)
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
//        _captureUiState.update {
//            it.copy(
//                billboardData = BillboardExtractedInfo(
//                    fileUri = eventSink.fileUri
//                )
//            )
//        }
        viewModelScope.launch(ioDispatcher) {

            val fileBitMap = fileSaver.getBitmapFromPath(eventSink.fileUri)

            if (fileBitMap.data != null) {
//                _captureUiState.update {
//                    it.copy(
//                        billboardData = it.billboardData?.copy(
//                            fullImage = fileBitMap.data!!
//                        )
//                    )
//                }

                val billboard = getBillboardSideToUpdate().copy(
                    billboardImage = fileBitMap.data!!,
                    closedUpUri = eventSink.fileUri
                )
                updateBillBoardState(billboard)

                _captureUiEvent.update {
                    CaptureRecordUiEvent.CaptureAdded
                }

                onAnalyseImage(CaptureRecordEventSink.OnAnalyseImage)
            }


        }

    }

    private suspend fun onAnalyseImage(eventSink: CaptureRecordEventSink.OnAnalyseImage) {
        // viewModelScope.launch(ioDispatcher) {
        if (_captureUiState.value.billboardData!!.closedUpUri != null) {
            val fileMultipart =
                buildMutipartBody(File(_captureUiState.value.billboardData!!.closedUpUri))
            _captureUiState.update {
                it.copy(
                    analysingLoading = true
                )
            }
            repository.analyzeFile(fileMultipart)
                .onSuccess { res ->
                    if (res?.object_type == null || res?.billboard_type == null)
                        _captureUiEvent.update {
                            CaptureRecordUiEvent.Error(
                                NoBillboardFoundException, eventSink
                            )
                        }

                    val billboard = getBillboardSideToUpdate()
                    val updatedBillboard = updateBillboardSideAiData(billboard, res)
                    updateBillBoardState(updatedBillboard)
//
//
//                    _captureUiState.update {
//                        it.copy(
//                            analysingLoading = false,
//                            billboardData = it.billboardData?.copy(
//                                brandName = res?.campaign_brand,
//                                brandSlogan = res?.campaign_description,
//                                brandCampaign = res?.campaign_description,
//                                objectType = res?.object_type,
//                                billboardWidth = res?.billboard_measurements?.width?.toString()
//                                    ?: "",
//                                billboardLength = res?.billboard_measurements?.height?.toString()
//                                    ?: "",
//                                phone = res?.contact_information?.phone,
//                                email = res?.contact_information?.email,
//                                siteUrl = res?.site_url,
//                                targetAge = res?.target_age,
//                                targetGender = res?.target_gender,
//                                campainSocials = res?.site_url,
//                                billboardType = res?.billboard_type,
//                            )
//                        )
//                    }
                }.onError {
                    println("Error...${it}")
                    _captureUiState.update {
                        it.copy(
                            analysingLoading = false
                        )
                    }
                }


        }
        //}
    }

    fun getBillboardSideToUpdate(): BillboardExtractedInfo{
        val uiData = _captureUiState.value
        var billBoardToUpdate: BillboardExtractedInfo? = null
        val sideBillboard = when(uiData.selectedBillboardSidesType){
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
        if (billBoardToUpdate == null){
            throw(NoBillboardFoundException)
        }
        return billBoardToUpdate
    }

    fun updateBillboardSideAiData(existingData: BillboardExtractedInfo, res: BBScoutAiAnalyserResponse?): BillboardExtractedInfo{
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
        if (billboard.billboardSideInfo != BillboardSides.MAIN && !billboard.billboardType.isNullOrEmpty()){
            billboard = billboard.copy(
                status = true
            )

        }
        return billboard

    }

    fun updateBillBoardState(billboardExtractedInfo: BillboardExtractedInfo){
        when (_captureUiState.value.selectedBillboardSidesType){
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
    }

    fun entityRecordFromBillboardExtractedInfo(info: BillboardExtractedInfo, userLocationEntity: UserLocationEntity): EntryRecord{
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
                visibility = info.visibility,
                illumination = info.illumination,
            ),
            otherData = emptyList(),
            location = userLocationEntity
        )
    }

}