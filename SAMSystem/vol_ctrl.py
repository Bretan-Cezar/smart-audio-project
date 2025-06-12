import time
from utils import VolumeCommand
import sys
from math import isclose

def range_limit(vol, min=0.0, max=1.0):
    if (vol.value < min):
        vol.value = min
    if (vol.value > max):
        vol.value = max


def volume_step(vol, step):
    vol.value += step
    range_limit(vol)


def volume_handler(GAIN_MIC, GAIN_MEDIA, q_volume_control, RELOAD_SIGNAL):

    print("Volume Handler Process started")

    step_vol_mic: float
    step_vol_media: float

    while not RELOAD_SIGNAL.value:

        try:

            volume_targets: VolumeCommand = q_volume_control.get() 

            print(f"Volume Handler received VolumeCommand[ GAIN_MEDIA_TARGET={volume_targets.GAIN_MEDIA_TARGET} ; GAIN_MIC_TARGET={volume_targets.GAIN_MIC_TARGET} ]\nAdjusting Volume...")

            if GAIN_MIC.value < volume_targets.GAIN_MIC_TARGET:
                step_vol_mic = 0.1
            elif GAIN_MIC.value > volume_targets.GAIN_MIC_TARGET:
                step_vol_mic = -0.1
            else:
                step_vol_mic = 0.0

            if GAIN_MEDIA.value < volume_targets.GAIN_MEDIA_TARGET:
                step_vol_media = 0.1
            elif GAIN_MEDIA.value > volume_targets.GAIN_MEDIA_TARGET:
                step_vol_media = -0.1
            else:
                step_vol_media = 0.0

            while not isclose(GAIN_MIC.value, volume_targets.GAIN_MIC_TARGET, rel_tol=1e-5) and not isclose(GAIN_MEDIA.value, volume_targets.GAIN_MEDIA_TARGET, rel_tol=1e-5):

                if GAIN_MIC.value != volume_targets.GAIN_MIC_TARGET:
                    volume_step(GAIN_MIC, step_vol_mic)

                if GAIN_MEDIA.value != volume_targets.GAIN_MEDIA_TARGET:
                    volume_step(GAIN_MEDIA, step_vol_media)
                
                time.sleep(0.1)
                
        except KeyboardInterrupt:
            print("Volume Handler Process stopped")
            sys.exit(15)

    sys.exit(0)

