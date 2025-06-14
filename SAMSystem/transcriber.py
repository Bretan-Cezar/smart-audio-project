import numpy as np
from utils import VolumeCommand
import sys
from vosk import Model, KaldiRecognizer
import json

def word_check(detection, phrase_list):
    return detection in phrase_list


def transcribe_buffer(buffer, pipeline):

    ret: str

    if pipeline.AcceptWaveform(buffer):
        ret = json.loads(pipeline.Result())["text"]

        pipeline.Reset()

        return ret
    else:
        ret = json.loads(pipeline.PartialResult())["partial"]

        pipeline.Reset()

        return ret


def transcription_handler(q_transcriber, q_volume_control, enabled):

    print("Transcriber Process started - Initializing model...")
    
    with open("config.json", "rt") as cfg:
        config = json.load(cfg)

    target_media_gain_enabled: float = float(config["mediaInputGainStateEnabled"])
    target_mic_gain_enabled: float = float(config["micInputGainStateEnabled"])

    phrase_list = list(config["phraseList"])
    phrase_list.append("[unk]")

    pipeline: KaldiRecognizer = KaldiRecognizer(Model(model_path=str(config["transcriberModelPath"])), config["transcriberSampleRate"], json.dumps(phrase_list))

    print("Transcriber Model Initialized!")

    while True:

        try:

            audio_buffer = q_transcriber.get()

            if audio_buffer is None:
                break

            audio_buffer = audio_buffer.tobytes()

            transcription = transcribe_buffer(audio_buffer, pipeline)

            print(f"Transcriber result: {transcription}")

            if transcription != "[unk]" and transcription in phrase_list:

                print(f"--SAM Toggled to ON--")

                q_volume_control.put(VolumeCommand(target_mic_gain_enabled, target_media_gain_enabled))

                enabled.value = True

        except KeyboardInterrupt:

            print("Transcriber Process exiting status 15...")
            sys.exit(15)

    print("Transcriber Process exiting status 0...")
    sys.exit(0)


