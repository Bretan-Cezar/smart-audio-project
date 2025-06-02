from faster_whisper import WhisperModel
import numpy as np
import soundfile as sf
from librosa import resample

if __name__ == "__main__":

    print("Transcriber Process started - Initializing model...")
    model = WhisperModel("tiny", device="cpu", compute_type="int8")
    print("Model initialized!")

    wave: np.ndarray

    with open("./recorded_audio.wav", "rb") as f:
        wave, sr = sf.read(f)
        wave = resample(wave.T, orig_sr=48000, target_sr=16000)

    segments, _ = model.transcribe(wave[0, :], vad_filter=False)

    result_text = ""

    for segment in segments:
        result_text += segment.text 

    print(result_text)

