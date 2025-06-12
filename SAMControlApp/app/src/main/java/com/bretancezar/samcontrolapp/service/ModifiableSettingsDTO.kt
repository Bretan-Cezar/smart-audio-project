package com.bretancezar.samcontrolapp.service

import kotlinx.serialization.Serializable

@Serializable
data class ModifiableSettingsDTO(
    val smartAmbienceMode: String,
    val phraseList: List<String>,
    val micInputGainStateEnabled: Float
)