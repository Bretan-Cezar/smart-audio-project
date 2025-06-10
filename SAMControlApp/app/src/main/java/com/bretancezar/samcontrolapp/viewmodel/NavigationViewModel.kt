package com.bretancezar.samcontrolapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.bretancezar.samcontrolapp.utils.Screens
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NavigationViewModel(
    startDestination: Screens,
    private val navController: NavController
): ViewModel() {

    val screenList = Screens.entries.toList()
    private var _currentScreen: MutableStateFlow<Screens> = MutableStateFlow(startDestination)
    val currentScreen: StateFlow<Screens> = _currentScreen.asStateFlow()

    fun setCurrentScreen(screen: Screens) {
        navController.navigate(screen.routeName)
        _currentScreen.value = screen
    }
}