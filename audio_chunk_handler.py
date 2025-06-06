import numpy as np
from librosa import resample
import sys
from torch import tensor, float32
from silero_vad.utils_vad import OnnxWrapper
from math import ceil
import json
import time
from utils import VolumeCommand

def silero_detect_chunk(model, chunk, sr: int) -> bool:
    return model(tensor(chunk, dtype=float32), sr).item() >= 0.5

def silero_detect_speech(model, buffer: np.ndarray, threshold: float, sr: int) -> bool:
    
    CHUNK_SIZE = 512
    THRESHOLD_SAMPLES = sr * threshold

    THRESHOLD_CHUNKS = ceil(THRESHOLD_SAMPLES / CHUNK_SIZE)

    COUNTER = 0

    for s_idx in range(0, buffer.shape[0], CHUNK_SIZE):

        w = buffer[s_idx:s_idx+CHUNK_SIZE]

        if w.shape[0] != CHUNK_SIZE:
            break

        if silero_detect_chunk(model, w, sr):
            COUNTER += 1
        else:
            COUNTER = 0

        if COUNTER == THRESHOLD_CHUNKS:
            return True
    
    return False

def enhance(buffer) -> np.ndarray:
    # TODO
    return buffer


def chunk_handler(block_size, q_chunks, q_transcriber, q_volume_control):
    print("Chunk Handler Process started. Initializing VAD model...")

    with open("config.json", "rt") as cfg:
        config = json.load(cfg)

    recording_sr = int(config["recordingSampleRate"])
    transcriber_sr = int(config["transcriberSampleRate"])

    buffer_duration = float(config["bufferDuration"])
    buffer_overlap = float(config["bufferOverlapDuration"])

    target_media_gain_disabled: float = float(config["mediaInputGainStateDisabled"])
    target_mic_gain_disabled: float = float(config["micInputGainStateDisabled"])

    model = OnnxWrapper(path=str(config["vadModelPath"]), force_onnx_cpu=True)

    buffer_size = int(buffer_duration * 48 * block_size)

    audio_buffer = np.empty((2, buffer_size), dtype=np.float32)
    length = 0

    enabled = False

    print("VAD Model Inintialized!")

    time_stamp = time.time()

    while True:

        try:
            audio_chunk = q_chunks.get()
            
            chunk_length = audio_chunk.shape[1]

            audio_buffer[:, length:length+chunk_length] = audio_chunk[:, :]

            length += chunk_length

            if length == buffer_size:

                audio_buffer_resampled = resample(audio_buffer, orig_sr=recording_sr, target_sr=transcriber_sr)

                audio_buffer_channel_mix = enhance((audio_buffer_resampled[0, :] + audio_buffer_resampled[1, :]) / 2.0)

                length = int((buffer_overlap*length)/buffer_duration)

                audio_buffer[:, :length] = audio_buffer[:, -length:]

                if (silero_detect_speech(model, audio_buffer_channel_mix, float(config["vadFilterPassDurationThreshold"]), transcriber_sr)):
                    print("--Speech detected--")

                    q_transcriber.put(audio_buffer_channel_mix.astype(np.float16))
                    
                    time_stamp = time.time()
                    enabled = True
                else:
                    curr_time = time.time()
                    if (curr_time - time_stamp >= 10.0 and enabled == True):
                        print("--SAM is turned off--")
                        q_volume_control.put(VolumeCommand(target_mic_gain_disabled, target_media_gain_disabled))
                        enabled = False

                print(q_transcriber.qsize())

        except KeyboardInterrupt:
            print("Chunk Handler Process stopped")
            sys.exit()           

