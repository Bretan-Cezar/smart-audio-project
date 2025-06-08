from dataclasses import dataclass

@dataclass
class VolumeCommand:
    GAIN_MIC_TARGET: float
    GAIN_MEDIA_TARGET: float

