package com.bretancezar.samcontrolapp.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationDefaults
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bretancezar.samcontrolapp.service.SmartAmbienceService
import com.bretancezar.samcontrolapp.ui.screen.DeviceScreen
import com.bretancezar.samcontrolapp.ui.screen.SmartAmbienceScreen
import com.bretancezar.samcontrolapp.ui.screen.SoundScreen
import com.bretancezar.samcontrolapp.utils.ScreenRouteName
import com.bretancezar.samcontrolapp.viewmodel.NavigationViewModel
import com.bretancezar.samcontrolapp.viewmodel.SmartAmbienceViewModel

@Composable
fun AppNavigation(
    applicationContext: Context
) {

    val navController = rememberNavController()
    val navigationViewModel = viewModel {  NavigationViewModel(ScreenRouteName.SMART_AMBIENCE) }
    val smartAmbienceViewModel = viewModel { SmartAmbienceViewModel(SmartAmbienceService(applicationContext)) }

    NavHost(navController = navController, startDestination = ScreenRouteName.SMART_AMBIENCE.routeName) {

        composable(
            route = ScreenRouteName.SMART_AMBIENCE.routeName
        ) {
            SmartAmbienceScreen()
        }

        composable(
            route = ScreenRouteName.SOUND.routeName
        ) {
            SoundScreen()
        }

        composable(
            route = ScreenRouteName.DEVICE.routeName
        ) {
            DeviceScreen()
        }
    }

}

@Composable
fun BottomNavigation(viewModel: NavigationViewModel) {

    val screenList: List<ScreenRouteName> = viewModel.screenList
    val selectedScreen by viewModel.currentScreen.collectAsState()

    BottomNavigation(
        windowInsets = BottomNavigationDefaults.windowInsets
    ) {
        screenList.forEachIndexed { _, screen ->
            BottomNavigationItem(
                icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                label = { Text(screen.displayName) },
                selected = selectedScreen == screen,
                onClick = { viewModel.setCurrentScreen(screen) }
            )
        }
    }
}