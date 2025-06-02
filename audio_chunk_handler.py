import numpy as np
from librosa import resample
import sys
import torch




def chunk_handler(block_size, q_chunks, q_transcriber):
    model, _ = torch.hub.load(repo_or_dir='snakers4/silero-vad',
                                  model='silero_vad',
                                  force_reload=True,
                                  onnx=True)

    print("Chunk Handler Process started")
    audio_buffer = np.empty((2, 4*48*block_size))
    length = 0

    while True:

        try:

            audio_chunk = q_chunks.get()
            
            chunk_length = audio_chunk.shape[1]

            audio_buffer[:, length:length+chunk_length] = audio_chunk[:, :]

            length += chunk_length

            if length >= 4*48*block_size:

                audio_buffer_resampled = resample(audio_buffer, orig_sr=48000, target_sr=16000)

                audio_buffer_channel_mix = ((audio_buffer_resampled[0, :] + audio_buffer_resampled[1, :])) / 2.0

                speech_probs = model(audio_buffer_channel_mix, 16000)
                cnt = torch.sum(speech_probs >= 0.5)
                if (cnt >= 8):
                    print("Speech detected: ")
                    audio_buffer[:, :((2*length)//4)] = audio_buffer[:, ((2*length)//4):]
                    length = (2*length)//4

                    q_transcriber.put(audio_buffer_channel_mix)

        except KeyboardInterrupt:
            print("Chunk Handler Process stopped")
            sys.exit()           

