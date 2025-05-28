import soundfile as sf
import numpy as np
import librosa
from faster_whisper import WhisperModel
import time
from threading import Thread

def range_limit(vol):
    if (vol.value < 0.0):
        vol.value = 0.0
    if (vol.value > 1.0):
        vol.value = 1.0

def volume_step(vol_mic, vol_media, step_vol):
    vol_mic.value += step_vol
    range_limit(vol_mic)

    vol_media.value -= step_vol
    range_limit(vol_media)

def word_check(sentence, target_word):
    word_chunk = sentence.replace(".", "").replace(",", "").split(' ').lower()
    for word in word_chunk:
        if target_word == word:
            return True

    return False

def transcribe_buffer(audio_buffer, model):
    segments, _ = model.transcribe(audio_buffer, beam_size=5)
    result_text = ""
    for segment in segments:
        result_text += segment.text

    return result_text

def volume_adjust_thread(audio_buffer_copy, model, vol_mic, vol_media, name):
    transcribe_text = transcribe_buffer(audio_buffer_copy, model)
    if (word_check(transcribe_text, name)):
        for i in range(5):
            volume_step(vol_mic, vol_media, 0.1)
            time.sleep(0.1)
def volume_change(vol_mic, vol_media, q, name):
    audio_buffer = np.empty(2, 48128)
    length = 0
    model = WhisperModel("tiny", device="cpu", compute_type="int8")
    while True:
        try:
            # volume = float(input("Enter increasing volume: "))
            audio_chunk = q.get()
            audio_buffer[length : length + audio_chunk.shape[0]] = audio_chunk
            length += audio_chunk.shape[0]
            if (length >= 48128):
                audio_buffer_copy = audio_buffer.copy()
                t1 = Thread(target = volume_adjust_thread, args = (audio_buffer_copy, model, vol_mic, vol_media, name), daemon=True)
                t1.start()
                audio_buffer[:length//2] = audio_buffer[length//2+1:length]

        except KeyboardInterrupt:
            break

if __name__ == "__main__":
    # processor = WhisperProcessor.from_pretrained("openai/whisper-small")
    # model = WhisperForConditionalGeneration.from_pretrained("openai/whisper-small")
    # model.config.forced_decoder_ids = None

    model = WhisperModel("tiny", device="cpu", compute_type="int8")
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


