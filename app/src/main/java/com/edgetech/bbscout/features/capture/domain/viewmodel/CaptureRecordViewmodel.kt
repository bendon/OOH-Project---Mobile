package com.edgetech.bbscout.features.capture.domain.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edgetech.bbscout.components.di.DefaultDispatcher
import com.edgetech.bbscout.components.di.IoDispatcher
import com.edgetech.bbscout.components.di.MainDispatcher
import com.edgetech.bbscout.components.file_saver.FileSaver
import com.edgetech.bbscout.components.location.GetLocationInfo
import com.edgetech.bbscout.components.utils.toLong
import com.edgetech.bbscout.data.data.local.dto.EntryRecord
import com.edgetech.bbscout.data.data.local.enities.BillboardDataEntity
import com.edgetech.bbscout.data.data.local.enities.EntryEntity
import com.edgetech.bbscout.data.data.local.enities.OtherDataEntity
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.FileResponse
import com.edgetech.bbscout.data.data.remote.gen_ai.llm.FulltextAndImageInference
import com.edgetech.bbscout.data.repositories.MainRepository
import com.edgetech.bbscout.data.utils.BBScoutException
import com.edgetech.bbscout.data.utils.SimpleResource
import com.edgetech.bbscout.data.utils.onError
import com.edgetech.bbscout.data.utils.onSuccess
import com.edgetech.bbscout.features.capture.domain.model.BillboardExtractedInfo
import com.edgetech.bbscout.features.capture.domain.model.BillboardTypeErrorException
import com.edgetech.bbscout.features.capture.domain.model.BrandDescriptionErrorException
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordEventSink
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiEvent
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiModel
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiState
import com.edgetech.bbscout.features.capture.domain.model.LocationErrorException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    // private var _billboardData: BillboardExtractedInfo? = null


    val uiModel = CaptureRecordUiModel(
        captureUiState = _captureUiState,
        captureUiEvent = _captureUiEvent
    ) { eventSink ->
        when (eventSink) {
            is CaptureRecordEventSink.OnCaptureEvent -> {

                _captureUiState.update {
                    it.copy(
                        billboardData = eventSink.billboardData
                    )
                }
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
            repository.getAllEntries().onSuccess { data ->
                _captureUiState.update {
                    it.copy(
                        allCaptures = data ?: emptyList()
                    )
                }
            }.onError { ex ->
                _captureUiEvent.update {
                    CaptureRecordUiEvent.Error(ex ?: BBScoutException("Unknown Error"), eventSink)
                }
            }
        }
    }

    private fun onSaveCapture(eventSink: CaptureRecordEventSink.OnSaveCapture) {

        viewModelScope.launch(ioDispatcher) {

//            var mainFile: File? = null
//            var billboardFile: File? = null
//
//            if (_captureUiState.value.billboardData?.fullImage != null) {
//                fileSaver.saveFile(_captureUiState.value.billboardData?.fullImage!!, "full")
//                    .onSuccess {
//                        mainFile = it
//                    }
//            }
//
//            if (_captureUiState.value.billboardData?.billboardImage != null) {
//                fileSaver.saveFile(
//                    _captureUiState.value.billboardData?.billboardImage!!,
//                    "billboard"
//                ).onSuccess {
//                    billboardFile = it
//                }.onError { ex ->
//
//                }
//            }


            var fullImageId: String? = null
            val captureFile = _captureUiState.value.billboardData?.fileUri


            if (_captureUiState.value.selectedLocation?.latitude == null || _captureUiState.value.selectedLocation?.longitude == null) {
                _captureUiEvent.update {
                    CaptureRecordUiEvent.Error(LocationErrorException, eventSink)
                }
                return@launch
            }

            if (_captureUiState.value.billboardData?.billboardType.isNullOrEmpty()) {
                _captureUiEvent.update {
                    CaptureRecordUiEvent.Error(BillboardTypeErrorException, eventSink)
                }
                return@launch
            }

            if (_captureUiState.value.billboardData?.brandCampaign.isNullOrEmpty()) {
                _captureUiEvent.update {
                    CaptureRecordUiEvent.Error(BrandDescriptionErrorException, eventSink)
                }
                return@launch
            }
            _captureUiState.update {
                it.copy(isLoading = true)
            }
            if (!captureFile.isNullOrEmpty()) {
                uploadImage(File(captureFile)).onSuccess {
                    fullImageId = it?.id
                }.onError { ex ->
                    _captureUiEvent.update {
                        CaptureRecordUiEvent.Error(ex ?: BBScoutException(), eventSink)
                    }
                    _captureUiState.update {
                        it.copy(isLoading = false)
                    }
                    return@launch
                }
            }

            val newEntry = EntryRecord(
                entryEntity = EntryEntity(
                    brand = _captureUiState.value.billboardData?.brandName,
                    rawText = _captureUiState.value.billboardData?.brandCampaign
                        ?: _captureUiState.value.billboardData?.rawText,
                    //mainFileUri = mainFile?.path,
                    //billboardFileUri = billboardFile?.path,
                    remoteFileId = fullImageId,
                    augmentedText = _captureUiState.value.billboardData?.brandCampaign,
                    createdAt = LocalDate.now().toLong(),
                    updatedAt = LocalDate.now().toLong(),
                ),
                otherData = (eventSink.qrCode?.map { OtherDataEntity(type = "QrCode", value = it) }
                    ?: emptyList()) +
                        (eventSink.entityInfos?.map {
                            OtherDataEntity(
                                type = it.type,
                                value = it.text
                            )
                        }
                            ?: emptyList()),
                location = _captureUiState.value.selectedLocation,
                billboardData = BillboardDataEntity(
                    height = _captureUiState.value.billboardData?.billboardLength?.toDoubleOrNull(),
                    width = _captureUiState.value.billboardData?.billboardWidth?.toDoubleOrNull(),
                    type = _captureUiState.value.billboardData?.billboardType,
                    owner = _captureUiState.value.billboardData?.billboardOwner
                )
            )
            repository.addEntryRecord(newEntry).onSuccess { result ->
                _captureUiEvent.update {
                    CaptureRecordUiEvent.CaptureRecordCreated(result)
                }
                _captureUiState.update {
                    it.copy(
                        selectedLocation = null,
                        billboardData = null
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

    private suspend fun uploadImage(file: File): SimpleResource<FileResponse> {
        val multipartBody = buildMutipartBody(file)
        return repository.uploadFile(multipartBody)
    }

    private fun buildMutipartBody(file: File): MultipartBody {
        return MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "file", file.asRequestBody())
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

    private fun onCapture(eventSink: CaptureRecordEventSink.OnCaptureEvent) {
        viewModelScope.launch(ioDispatcher) {
            _captureUiState.update {
                it.copy(
                    analysingLoading = true
                )
            }
            val fileBitMap = fileSaver.getBitmapFromPath(eventSink.billboardData.fileUri!!)

            if (fileBitMap.data != null) {
                _captureUiState.update {
                    it.copy(
                        billboardData = eventSink.billboardData.copy(
                            fullImage = fileBitMap.data
                        )
                    )
                }
                _captureUiEvent.update {
                    CaptureRecordUiEvent.CaptureAdded
                }

                val fileMultipart = buildMutipartBody(File(eventSink.billboardData.fileUri))
                repository.analyzeFile(fileMultipart)
                    .onSuccess { res ->
                        _captureUiState.update {
                            it.copy(
                                billboardData = it.billboardData?.copy(
                                    brandName = res?.campaign_brand,
                                    brandSlogan = res?.campaign_description,
                                    brandCampaign = res?.campaign_description,
                                    billboardWidth = res?.billboard_measurements?.width?.toString()
                                        ?: "",
                                    billboardLength = res?.billboard_measurements?.height?.toString()
                                        ?: "",
                                )
                            )
                        }
                    }

//                llmInference.getCampaignInfo(eventSink.billboardData.fullImage, "")
//                    .onSuccess { campaign ->
//
//                        _captureUiState.update {
//                            it.copy(
//                                billboardData = it.billboardData?.copy(
//                                    brandName = campaign?.brand,
//                                    brandSlogan = campaign?.slogan,
//                                    brandCampaign = campaign?.campaignTheme
//                                )
//                            )
//                        }
//                    }
            }
            _captureUiState.update {
                it.copy(
                    analysingLoading = false
                )
            }
        }
    }

}