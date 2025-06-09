package com.bretancezar.samcontrolapp.viewmodel

import androidx.lifecycle.ViewModel
import com.bretancezar.samcontrolapp.utils.ScreenRouteName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NavigationViewModel(
    startDestination: ScreenRouteName
): ViewModel() {

    val screenList = ScreenRouteName.entries.toList()

    private var _currentScreen: MutableStateFlow<ScreenRouteName> = MutableStateFlow(startDestination)
    val currentScreen: StateFlow<ScreenRouteName> = _currentScreen.asStateFlow()

    fun setCurrentScreen(screen: ScreenRouteName) {

        _currentScreen.value = screen
    }
}