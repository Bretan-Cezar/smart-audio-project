package com.bretancezar.samcontrolapp.viewmodel

import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bretancezar.samcontrolapp.service.ServiceException
import com.bretancezar.samcontrolapp.service.SmartAmbienceService
import com.bretancezar.samcontrolapp.utils.SmartAmbienceMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SmartAmbienceViewModel(
    private val smartAmbienceService: SmartAmbienceService
): ViewModel() {

    val modeList: List<SmartAmbienceMode> = SmartAmbienceMode.entries.toList()

    private var _selectedMode: MutableStateFlow<SmartAmbienceMode?> = MutableStateFlow(null)
    var selectedMode: StateFlow<SmartAmbienceMode?> = _selectedMode.asStateFlow()

    private var _phraseList: MutableStateFlow<List<String>> = MutableStateFlow(listOf())

    var phraseList: StateFlow<List<String>> =
        _phraseList.asStateFlow()

    private var _awaitingResponse: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val awaitingResponse: StateFlow<Boolean> = _awaitingResponse.asStateFlow()

    var isConnected: Boolean = false

    init {
        viewModelScope.launch(Dispatchers.IO) {

            while (true) {

                val check = smartAmbienceService.isConnected()
                if (check != isConnected) {

                    if (smartAmbienceService.isConnected()) {
                        _selectedMode.value = smartAmbienceService.getCurrentMode()
                        _phraseList.value = smartAmbienceService.getCurrentPhraseList()
                    }
                    else {
                        _selectedMode.value = null
                        _phraseList.value = listOf()
                    }

                    isConnected = check
                }

                delay(1000)
            }
        }
    }

    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun setSelectedMode(smartAmbienceMode: SmartAmbienceMode, onErrorAction: () -> Unit) {
        _awaitingResponse.value = true

        viewModelScope.launch(Dispatchers.IO) {

            try {
                smartAmbienceService.setMode(smartAmbienceMode)
                _awaitingResponse.value = false
            }
            catch (se: ServiceException) {
                _awaitingResponse.value = false
                onErrorAction()
            }

            _selectedMode.value = smartAmbienceMode
        }
    }

    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun addPhrase(phrase: String, onErrorAction: () -> Unit) {
        _awaitingResponse.value = true

        viewModelScope.launch(Dispatchers.IO) {

            val newList = _phraseList.value + phrase

            try {
                smartAmbienceService.setPhraseList(newList)
                _awaitingResponse.value = false
            }
            catch (se: ServiceException) {
                _awaitingResponse.value = false
                onErrorAction()
            }
            _phraseList.value = newList
        }
    }

    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun deletePhrase(phrase: String, onErrorAction: () -> Unit) {
        _awaitingResponse.value = true

        viewModelScope.launch(Dispatchers.IO) {

            val newList = _phraseList.value.filter { it != phrase }

            try {
                smartAmbienceService.setPhraseList(newList)
                _awaitingResponse.value = false
            }
            catch (se: ServiceException) {
                _awaitingResponse.value = false
                onErrorAction()
            }
            _phraseList.value = newList
        }
    }
}