package com.bretancezar.samcontrolapp.service

data class ModifiableSettingsDTO(
    val smartAmbienceMode: String,
    val phraseList: List<String>,
    val micInputGainStateEnabled: Float
)