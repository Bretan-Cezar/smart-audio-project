# Import library
import sounddevice as sd
import soundfile as sf
from ffmpeg_normalize import FFmpegNormalize

duration = 2  # 10 seconds of recording audio from environment
samplerate = 16000  # 16 kHz sampling rate
output_file = 'recorded_audio.wav'
output_norm_file = 'recorded_audio_normalized.wav'

print("Recording from environment...")

# Record audio
recording = sd.rec(int(samplerate * duration), samplerate=samplerate, channels=2, dtype='float32')
sd.wait()  # Wait until recording is finished

# Save to a WAV file
sf.write(output_file, recording, samplerate)

print("Original file written to disk. Normalizing...")

recording = FFmpegNormalize()
