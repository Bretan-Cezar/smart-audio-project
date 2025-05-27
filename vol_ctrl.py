import torch
from transformers import WhisperProcessor, WhisperForConditionalGeneration
import soundfile as sf
import numpy as np

def range_limit(vol):
    if (vol.value < 0.0):
        vol.value = 0.0
    if (vol.value > 1.0):
        vol.value = 1.0

def volume_change(vol_mic, vol_media):
    while True:
        try:
            volume = float(input("Enter increasing volume: "))
            
            vol_mic.value += volume
            range_limit(vol_mic)
            
            vol_media.value -= volume
            range_limit(vol_media)

        except KeyboardInterrupt:
            break

if __name__ == "__main__":
    processor = WhisperProcessor.from_pretrained("openai/whisper-small")
    model = WhisperForConditionalGeneration.from_pretrained("openai/whisper-small")
    model.config.forced_decoder_ids = None

    sample: np.ndarray

    with open("./recorded_audio.wav", "rb") as f:
        sample, sr = sf.read(f)

    print(sample.shape)

        
