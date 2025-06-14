package com.bretancezar.samcontrolapp.viewmodel

import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bretancezar.samcontrolapp.service.SmartAmbienceService
import com.bretancezar.samcontrolapp.utils.SmartAmbienceMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SmartAmbienceViewModel(
    private val smartAmbienceService: SmartAmbienceService
): ViewModel() {

    val modeList: List<SmartAmbienceMode> = SmartAmbienceMode.entries.toList()

    private var _selectedMode: MutableStateFlow<SmartAmbienceMode?> = MutableStateFlow(smartAmbienceService.getCurrentMode())
    var selectedMode: StateFlow<SmartAmbienceMode?> = _selectedMode.asStateFlow()

    private var _phraseList: MutableStateFlow<List<String>> =
        MutableStateFlow(smartAmbienceService.getCurrentPhraseList())

    var phraseList: StateFlow<List<String>> =
        _phraseList.asStateFlow()

    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun setSelectedMode(smartAmbienceMode: SmartAmbienceMode) {

        viewModelScope.launch(Dispatchers.IO) {

            smartAmbienceService.setMode(smartAmbienceMode)

            _selectedMode.value = smartAmbienceMode
        }
    }

    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun addPhrase(phrase: String) {

        viewModelScope.launch(Dispatchers.IO) {

            val newList = _phraseList.value + phrase

            smartAmbienceService.setPhraseList(newList)

            _phraseList.value = newList
        }
    }

    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun deletePhrase(phrase: String) {

        viewModelScope.launch(Dispatchers.IO) {

            val newList = _phraseList.value.filter { it != phrase }

            smartAmbienceService.setPhraseList(newList)

            _phraseList.value = newList
        }
    }
}