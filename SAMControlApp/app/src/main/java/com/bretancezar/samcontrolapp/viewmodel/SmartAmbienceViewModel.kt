package com.bretancezar.samcontrolapp.viewmodel

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

    fun setSelectedMode(smartAmbienceMode: SmartAmbienceMode) {
        _selectedMode.value = smartAmbienceMode

        // smartAmbienceService.setMode()
    }

}