package com.edgetech.bbscout.features.capture.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edgetech.bbscout.components.di.DefaultDispatcher
import com.edgetech.bbscout.components.di.IoDispatcher
import com.edgetech.bbscout.components.di.MainDispatcher
import com.edgetech.bbscout.components.utils.toLong
import com.edgetech.bbscout.data.data.local.dto.EntryRecord
import com.edgetech.bbscout.data.data.local.enities.BillboardDataEntity
import com.edgetech.bbscout.data.data.local.enities.EntryEntity
import com.edgetech.bbscout.data.data.local.enities.OtherDataEntity
import com.edgetech.bbscout.data.repositories.MainRepository
import com.edgetech.bbscout.data.utils.BBScoutException
import com.edgetech.bbscout.data.utils.onError
import com.edgetech.bbscout.data.utils.onSuccess
import com.edgetech.bbscout.features.capture.domain.model.BillboardExtractedInfo
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordEventSink
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiEvent
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiModel
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject


@HiltViewModel
class CaptureRecordViewmodel @Inject constructor(
    private val repository: MainRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _captureUiState = MutableStateFlow(
        CaptureRecordUiState()
    )
    private val _captureUiEvent = MutableStateFlow<CaptureRecordUiEvent>(CaptureRecordUiEvent.Empty)

    private var _billboardData: BillboardExtractedInfo? = null


    val uiModel = CaptureRecordUiModel(
        captureUiState = _captureUiState,
        captureUiEvent = _captureUiEvent
    ) { eventSink ->
        when (eventSink) {
            is CaptureRecordEventSink.OnCaptureEvent -> {
                _billboardData = eventSink.billboardData
                _captureUiState.update {
                    it.copy(
                        billboardData = eventSink.billboardData
                    )
                }
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

        val newEntry = EntryRecord(
            entryEntity = EntryEntity(
                brand = eventSink.brandName,
                rawText = _billboardData?.rawText,
                mainFileUri = eventSink.mainFileUri,
                billboardFileUri = eventSink.billboardFileUri,
                augmentedText = eventSink.advertDescription,
                createdAt = LocalDate.now().toLong(),
                updatedAt = LocalDate.now().toLong(),
            ),
            otherData = (eventSink.qrCode?.map { OtherDataEntity(type = "QrCode", value = it) }
                ?: emptyList()) +
                    (eventSink.entityInfos?.map { OtherDataEntity(type = it.type, value = it.text) }
                        ?: emptyList()),
            location = eventSink.location,
            billboardData = eventSink.billboardData
        )


        viewModelScope.launch(ioDispatcher) {
            repository.addEntryRecord(newEntry).onSuccess {result ->
                _captureUiEvent.update {
                    CaptureRecordUiEvent.CaptureRecordCreated(result ?: 0)
                }
            }.onError { ex ->
                _captureUiEvent.update {
                    CaptureRecordUiEvent.Error(ex ?: BBScoutException("Unknown Error"), eventSink)
                }
            }
        }
    }

    private fun onGetAllCaptures(eventSink: CaptureRecordEventSink.OnGetAllCaptures) {
        viewModelScope.launch(ioDispatcher) {
            repository.getAllEntries().onSuccess { data ->
                _captureUiState.update {
                    it.copy(
                        allCaptures = data?.take(5) ?: emptyList()
                    )
                }
            }.onError { ex ->
                _captureUiEvent.update {
                    CaptureRecordUiEvent.Error(ex ?: BBScoutException("Unknown Error"), eventSink)
                }
            }
        }
    }

    private fun onCapture(eventSink: CaptureRecordEventSink.OnCaptureEvent) {
        TODO("Not yet implemented")
    }

}