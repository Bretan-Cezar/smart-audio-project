package com.bretancezar.samcontrolapp.utils

import com.bretancezar.samcontrolapp.R

enum class Screens(val routeName: String, val displayName: String, val icon: Int) {
    FIRST_SCREEN("first_screen","First Screen", R.drawable.headphones_24px),
    SOUND("sound", "Sound", R.drawable.volume_up_24px),
    SMART_AMBIENCE("smart_ambience", "Smart Ambience", R.drawable.noise_control_on_24px),
    DEVICE("device", "Device", R.drawable.baseline_headphones_24),
}