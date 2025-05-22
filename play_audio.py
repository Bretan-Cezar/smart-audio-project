import sounddevice as sd
import soundfile as sf

with open("./recorded_audio.wav", "rb") as f:
    recording, samplerate = sf.read(f)

audio, fs = recording, samplerate
# Play the audio
print("Playing the audio from environment...")
sd.play(audio,fs)
sd.wait()