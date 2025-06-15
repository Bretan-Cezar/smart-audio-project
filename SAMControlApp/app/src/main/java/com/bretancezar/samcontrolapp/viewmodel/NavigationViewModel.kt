package com.bretancezar.samcontrolapp.viewmodel

import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.bretancezar.samcontrolapp.service.ServiceException
import com.bretancezar.samcontrolapp.service.SmartAmbienceService
import com.bretancezar.samcontrolapp.utils.Screens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NavigationViewModel(
    private val navController: NavController,
    private val smartAmbienceService: SmartAmbienceService
): ViewModel() {

    val screenList = Screens.entries.filter{ it != Screens.FIRST_SCREEN }.toList()

    private var _currentScreen: MutableStateFlow<Screens> = MutableStateFlow(Screens.FIRST_SCREEN)
    val currentScreen: StateFlow<Screens> = _currentScreen.asStateFlow()

    private var _awaitingResponse: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val awaitingResponse: StateFlow<Boolean> = _awaitingResponse.asStateFlow()

    var isConnected: Boolean = false

    init {
        viewModelScope.launch {

            while (true) {

                if (navController.currentDestination != null) {

                    val check = smartAmbienceService.isConnected()

                    if (check != isConnected) {

                        if (check) {
                            setCurrentScreen(Screens.SMART_AMBIENCE)
                        }
                        else {
                            setCurrentScreen(Screens.FIRST_SCREEN)
                        }

                        isConnected = check
                    }
                }
                delay(1000)
            }
        }
    }

    fun setCurrentScreen(screen: Screens) {

//        viewModelScope.launch(Dispatchers.Main) {
//            while (_currentScreen.value != screen) {
//                try {
        navController.navigate(screen.routeName)
        _currentScreen.value = screen
//                }
//                catch (iae: IllegalArgumentException) {
//                    Log.i("nav", "pula")
//                    delay(1000)
//                }
//            }
//        }
    }

    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun attemptConnection(onErrorAction: (String) -> Unit) {
        _awaitingResponse.value = true

        viewModelScope.launch(Dispatchers.IO) {

            try {
                smartAmbienceService.init()
                _awaitingResponse.value = false
            }
            catch (se: ServiceException) {
                _awaitingResponse.value = false
                onErrorAction(se.type.displayMsg)
            }
        }
    }
}