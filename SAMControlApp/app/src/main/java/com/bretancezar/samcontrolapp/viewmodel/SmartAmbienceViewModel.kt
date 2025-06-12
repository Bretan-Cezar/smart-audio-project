package com.bretancezar.samcontrolapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bretancezar.samcontrolapp.utils.SmartAmbienceMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SmartAmbienceViewModel(
    private val smartAmbienceService: Any
): ViewModel() {

    val modeList: List<SmartAmbienceMode> = SmartAmbienceMode.entries.toList()

    private var _selectedMode: MutableStateFlow<SmartAmbienceMode?> = MutableStateFlow(null)
    var selectedMode: StateFlow<SmartAmbienceMode?> = _selectedMode.asStateFlow()

    private var _phraseList: MutableStateFlow<List<String>> =
        MutableStateFlow(listOf())

    var phraseList: StateFlow<List<String>> =
        _phraseList.asStateFlow()

    fun setSelectedMode(smartAmbienceMode: SmartAmbienceMode) {
        _selectedMode.value = smartAmbienceMode

        // smartAmbienceService.setMode()
    }

    fun addPhrase(phrase: String) {

        val copy = _phraseList.value.toMutableSet()
        copy.add(phrase)

        _phraseList.value = copy.toList()
    }

    fun deletePhrase(phrase: String) {
        _phraseList.value = _phraseList.value.filter { it != phrase }
    }
}