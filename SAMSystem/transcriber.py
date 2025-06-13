import numpy as np
from utils import VolumeCommand
import sys
from vosk import Model, KaldiRecognizer
from queue import ShutDown
import json

def word_check(detection, phrase_list):
    return detection in phrase_list


def transcribe_buffer(buffer, pipeline):

    if pipeline.AcceptWaveform(buffer):
        res = json.loads(pipeline.Result())
        return res["text"]
    else:
        res = json.loads(pipeline.PartialResult())
        return res["partial"]


def transcription_handler(q_transcriber, q_volume_control, RELOAD_SIGNAL):

    print("Transcriber Process started - Initializing model...")
    
    with open("config.json", "rt") as cfg:
        config = json.load(cfg)

    target_media_gain_enabled: float = float(config["mediaInputGainStateEnabled"])
    target_mic_gain_enabled: float = float(config["micInputGainStateEnabled"])

    phrase_list = list(config["phraseList"])

    pipeline = KaldiRecognizer(Model(model_path=str(config["transcriberModelPath"])), config["transcriberSampleRate"], json.dumps(phrase_list))

    print("Transcriber Model Initialized!")

    while True:

        try:

            try:
                audio_buffer = q_transcriber.get().tobytes()
            except ShutDown:
                break

            transcription = transcribe_buffer(audio_buffer, pipeline)

            print(transcription)

            if transcription in phrase_list:

                print(f"NAME FOUND - issuing volume command...")
                q_volume_control.put(VolumeCommand(target_mic_gain_enabled, target_media_gain_enabled))

        except KeyboardInterrupt:

            print("Transcriber Process exiting status 15...")
            sys.exit(15)

    print("Transcriber Process exiting status 0...")
    sys.exit(0)


