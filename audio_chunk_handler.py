import numpy as np
from librosa import resample
import sys
import torch
from math import ceil

def silero_detect_chunk(model, chunk, sr: int = 16000) -> bool:
    return model(torch.tensor(chunk, dtype=torch.float32), sr).item() >= 0.5

def silero_detect_speech(model, buffer: np.ndarray, threshold: float = 0.25, sr: int = 16000) -> bool:
    
    CHUNK_SIZE = 512
    THRESHOLD_SAMPLES = sr * threshold

    THRESHOLD_CHUNKS = ceil(THRESHOLD_SAMPLES / CHUNK_SIZE)

    COUNTER = 0

    for s_idx in range(0, buffer.shape[0], CHUNK_SIZE):

        w = buffer[s_idx:s_idx+CHUNK_SIZE]

        if w.shape[0] != CHUNK_SIZE:
            break

        if silero_detect_chunk(model, w):
            COUNTER += 1
        else:
            COUNTER = 0

        if COUNTER == THRESHOLD_CHUNKS:
            return True
    
    return False


def chunk_handler(block_size, q_chunks, q_transcriber):
    model, _ = torch.hub.load(repo_or_dir='snakers4/silero-vad',
                                  model='silero_vad',
                                  force_reload=True,
                                  onnx=True)

    print("Chunk Handler Process started")
    audio_buffer = np.empty((2, 4*48*block_size), dtype=np.float32)
    length = 0

    while True:

        try:

            audio_chunk = q_chunks.get()
            
            chunk_length = audio_chunk.shape[1]

            audio_buffer[:, length:length+chunk_length] = audio_chunk[:, :]

            length += chunk_length

            if length >= 4*48*block_size:

                audio_buffer_resampled = resample(audio_buffer, orig_sr=48000, target_sr=16000)

                audio_buffer_channel_mix = ((audio_buffer_resampled[0, :] + audio_buffer_resampled[1, :])) / 4.0

                if (silero_detect_speech(model, audio_buffer_channel_mix)):
                    print("--Speech detected--")
                    audio_buffer[:, :((2*length)//4)] = audio_buffer[:, ((2*length)//4):]
                    length = (2*length)//4

                    q_transcriber.put(audio_buffer_channel_mix)
                    print(q_transcriber.qsize())

        except KeyboardInterrupt:
            print("Chunk Handler Process stopped")
            sys.exit()           

