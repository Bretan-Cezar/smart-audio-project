package com.bretancezar.samcontrolapp.utils
import com.bretancezar.samcontrolapp.R
import androidx.compose.ui.graphics.Color
import com.bretancezar.samcontrolapp.ui.theme.Green
import com.bretancezar.samcontrolapp.ui.theme.Red
import com.bretancezar.samcontrolapp.ui.theme.Yellow

enum class SmartAmbienceMode(val icon: Int, val displayTitle: String, val description: String, val color: Color) {
    LIGHT(
        R.drawable.desktop_windows_24px,
        "Light Work",
        "Respond to name 1 time",
        Green),
    MODERATE(
        R.drawable.group_24px,
        "Focus Mode",
        "Respond to name 2 times",
        Yellow),
    DEEP(
        R.drawable.neurology_24px,
        "Deep Focus",
        "No responsiveness to name",
        Red)
}