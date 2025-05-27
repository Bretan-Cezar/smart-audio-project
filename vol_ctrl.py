import soundfile as sf
import numpy as np
import librosa
from faster_whisper import WhisperModel

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
    # processor = WhisperProcessor.from_pretrained("openai/whisper-small")
    # model = WhisperForConditionalGeneration.from_pretrained("openai/whisper-small")
    # model.config.forced_decoder_ids = None

    model = model = WhisperModel("tiny", device="cpu", compute_type="int8")
    sample: np.ndarray

    with open("./recorded_audio.wav", "rb") as f:
        sample, sr = sf.read(f)
        # sample = librosa.resample(sample, orig_sr = sr, target_sr = 16000)

    # input_features = processor(sample[:,0], sampling_rate = 16000, return_tensors="pt").input_features
    # predicted_ids = model.generate(input_features)
    # transcription = processor.batch_decode(predicted_ids, skip_special_tokens=True)

    print("Model initialized")
    print("Transcription #1...")
    segments, info = model.transcribe("recorded_audio.wav", beam_size=5)
    for segment in segments:
        print("[%.2fs -> %.2fs] %s" % (segment.start, segment.end, segment.text))
    

    print("Transcription #2...")
    segments, info = model.transcribe("recorded_audio.wav", beam_size=5)
    for segment in segments:
        print("[%.2fs -> %.2fs] %s" % (segment.start, segment.end, segment.text))


