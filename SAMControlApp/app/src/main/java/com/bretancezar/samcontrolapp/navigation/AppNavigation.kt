package com.bretancezar.samcontrolapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "smart_ambience") {

        composable(
            route = "smart_ambience"
        ) {

        }

        composable(
            route = "sound"
        ) {

        }

        composable(
            route = "device"
        ) {

        }
    }
}