# Import library
import numpy as np
from scipy.fftpack import fft
from scipy import signal
import sounddevice as sd
import soundfile as sf

duration = 3  # 3 seconds of recording audio from environment
samplerate = 16000  # 16 kHz sampling rate
output_file = 'recorded_audio.wav'

# print("Recording from environment...")
# 
# # Record audio
# recording = sd.rec(int(samplerate * duration), samplerate=samplerate, channels=1, dtype='int16')
# sd.wait()  # Wait until recording is finished
# 
# # Save to a WAV file
# sf.write(output_file, recording, samplerate)
# 
# print(f"Recording saved as {output_file}")

with open("./recorded_audio.wav", "rb") as f:
    recording, samplerate = sf.read(f)

audio, fs = recording, samplerate
# Play the audio
print("Playing the audio from environment...")
sd.play(audio,fs)
sd.wait()
