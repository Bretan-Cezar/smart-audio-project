import time
from utils import VolumeCommand
import sys

def range_limit(vol, min=0.0, max=1.0):
    if (vol.value < min):
        vol.value = min
    if (vol.value > max):
        vol.value = max


def volume_step(vol, step):
    vol.value += step
    range_limit(vol)


def volume_handler(vol_mic, vol_media, q_volume_control):

    print("Volume Handler Process started")

    step_vol_mic: float
    step_vol_media: float

    while True:

        try:

            volume_targets: VolumeCommand = q_volume_control.get() 

            print(f"Volume Handler received VolumeCommand[ GAIN_MEDIA_TARGET={volume_targets.GAIN_MEDIA_TARGET} ; GAIN_MIC_TARGET={volume_targets.GAIN_MIC_TARGET} ]")

            if vol_mic.value < volume_targets.GAIN_MIC_TARGET:
                step_vol_mic = 0.1
            elif vol_mic.value > volume_targets.GAIN_MIC_TARGET:
                step_vol_mic = -0.1
            else:
                step_vol_mic = 0.0

            if vol_media.value < volume_targets.GAIN_MEDIA_TARGET:
                step_vol_media = 0.1
            elif vol_media.value > volume_targets.GAIN_MEDIA_TARGET:
                step_vol_media = -0.1
            else:
                step_vol_media = 0.0

            while vol_mic.value != volume_targets.GAIN_MIC_TARGET and vol_media.value != volume_targets.GAIN_MEDIA_TARGET:

                if vol_mic.value != volume_targets.GAIN_MIC_TARGET:
                    volume_step(vol_mic, step_vol_mic)

                if vol_media.value != volume_targets.GAIN_MEDIA_TARGET:
                    volume_step(vol_media, step_vol_media)
                
                time.sleep(0.1)
                
        except KeyboardInterrupt:
            print("Volume Handler Process stopped")
            sys.exit()

