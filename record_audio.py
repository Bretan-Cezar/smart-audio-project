# Import library
import sounddevice as sd
import soundfile as sf

duration = 10  # 10 seconds of recording audio from environment
samplerate = 48000  # 16 kHz sampling rate
output_file = 'recorded_audio.wav'

print("Recording from environment...")

# Record audio
recording = sd.rec(int(samplerate * duration), samplerate=samplerate, channels=2, dtype='float32')
sd.wait()  # Wait until recording is finished

# Save to a WAV file
sf.write(output_file, recording, samplerate)
