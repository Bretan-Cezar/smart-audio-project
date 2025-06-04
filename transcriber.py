import numpy as np
from utils import VolumeCommand
import sys
from vosk import Model, KaldiRecognizer
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


def transcription_handler(phrase_list, q_transcriber, q_volume_control):

    print("Transcriber Process started - Initializing model...")
    
    with open("config.json", "rt") as cfg:
        config = json.load(cfg)

    target_media_gain_enabled: float = float(config["mediaInputGainStateEnabled"])
    target_mic_gain_enabled: float = float(config["micInputGainStateEnabled"])

    target_media_gain_disabled: float = float(config["mediaInputGainStateDisabled"])
    target_mic_gain_disabled: float = float(config["micInputGainStateDisabled"])

    pipeline = KaldiRecognizer(Model(model_path=str(config["transcriberModelPath"])), 16000, json.dumps(phrase_list))

    print("Transcriber Model Initialized!")

    # TODO Use a timestamp to mark when the SAM was active, maybe after 10 secs of non-detection, turn SAM back off

    while True:

        try:

            audio_buffer = q_transcriber.get().tobytes()

            transcription = transcribe_buffer(audio_buffer, pipeline)

            print(transcription)

            if transcription in phrase_list:

                print(f"NAME FOUND - issuing volume command...")
                q_volume_control.put(VolumeCommand(target_mic_gain_enabled, target_media_gain_enabled))

        except KeyboardInterrupt:

            print("Transcriber Process stopped")
            sys.exit()
