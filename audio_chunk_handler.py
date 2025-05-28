import numpy as np
from librosa import resample
import sys

def chunk_handler(block_size, q_chunks, q_transcriber):

    print("Chunk Handler Process started")
    audio_buffer = np.empty((2, 10*48*block_size))
    length = 0

    while True:

        try:

            audio_chunk = q_chunks.get()
            
            chunk_length = audio_chunk.shape[1]

            audio_buffer[:, length:length+chunk_length] = audio_chunk[:, :]

            length += chunk_length

            if length >= 10*48*block_size:

                audio_buffer_resampled = resample(audio_buffer, orig_sr=48000, target_sr=16000)

                audio_buffer_channel_mix = ((audio_buffer_resampled[0, :] + audio_buffer_resampled[1, :])) / 2

                audio_buffer[:, :((5*length)//10)] = audio_buffer[:, ((5*length)//10):]
                length = (5*length)//10

                q_transcriber.put(audio_buffer_channel_mix)

        except KeyboardInterrupt:
            print("Chunk Handler Process stopped")
            sys.exit()           

