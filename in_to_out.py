#!/usr/bin/env python3
"""Pass input directly to output.

https://app.assembla.com/spaces/portaudio/git/source/master/test/patest_wire.c

"""
import json

import sounddevice as sd
import numpy  # Make sure NumPy is loaded before it is used in the callback


config: dict
SR: int
INPUT_DEVICE_INDEX: int
OUTPUT_DEVICE_INDEX: int
BUFFER_SIZE: int
DTYPE: str
MODEL = None

if __name__ == "__main__":
    with open("config.json", "rt") as cfg:
        config = json.load(cfg)
    
    SR = int(config["sampleRate"])
    INPUT_DEVICE_INDEX = int(config["inDeviceIndex"])
    OUTPUT_DEVICE_INDEX = int(config["outDeviceIndex"])
    BLOCK_SIZE = int(config["blockSize"])
    DTYPE = config["dtype"]

    def callback(indata, outdata, frames, time, status):
        if status:
            print(status)
        outdata[:] = indata
    
    try:
        with sd.Stream(device=(INPUT_DEVICE_INDEX, OUTPUT_DEVICE_INDEX), samplerate=SR, blocksize=BLOCK_SIZE, dtype=DTYPE, latency="high", channels=1, callback=callback):
            print('#' * 8)
            print('press Return to quit')
            print('#' * 8)
            input()

    except KeyboardInterrupt:
        exit()
    except Exception as e:
        exit()
