import numpy as np
import argparse
import queue
import sys
import sounddevice as sd
from librosa import resample
from librosa.util import buf_to_float
from vosk import Model, KaldiRecognizer
# print(sd.query_devices())

q = queue.Queue()

def int_or_str(text):
    """Helper function for argument parsing."""
    try:
        return int(text)
    except ValueError:
        return text

def callback(indata, frames, time, status):
    """This is called (from a separate thread) for each audio block."""
    if status:
        print(status, file=sys.stderr)

    indata = buf_to_float(indata, dtype=np.float16)

    q.put(indata.tobytes())

try:
        
    model = Model(lang="en-us")

    with sd.RawInputStream(samplerate=48000, blocksize = 8000, device=1,
            dtype="int16", channels=1, callback=callback):

        print("Press Ctrl+C to stop the recording")

        rec = KaldiRecognizer(model, 16000)

        while True:

            data = q.get()

            if rec.AcceptWaveform(data):
                print(rec.Result())

            else:
                print(rec.PartialResult())


except KeyboardInterrupt:
    print("\nDone")
    exit(0)

except Exception as e:
    exit(1)
