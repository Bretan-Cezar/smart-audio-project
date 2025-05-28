import numpy as np

def chunk_handler(q_chunks, q_transcriber):

    print("Chunk Handler Process started")
    audio_buffer = np.empty((2, 49152))
    length = 0

    while True:

        try:

            audio_chunk = q_chunks.get()
            
            chunk_length = audio_chunk.shape[1]

            audio_buffer[:, length:length+chunk_length] = audio_chunk[:, :]

            length += chunk_length

            if (length >= 49152):

                audio_buffer_copy = audio_buffer.copy()

                audio_buffer[:, :length//2] = audio_buffer[:, length//2:length]
                length = length // 2

                q_transcriber.put(audio_buffer_copy)

        except KeyboardInterrupt:
            print("Chunk Handler Process stopped")
            break

