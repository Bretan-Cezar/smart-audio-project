package com.bretancezar.samcontrolapp.service

import com.bretancezar.samcontrolapp.utils.SmartAmbienceMode

class DeviceSettings(
    var smartAmbienceMode: SmartAmbienceMode,
    var phraseList: List<String>,
    var micInputGainStateEnabled: Float
)