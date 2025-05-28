from faster_whisper import WhisperModel
from utils import VolumeCommand

def word_check(sentence, target_word):
    word_chunk = sentence.lower().replace(".", "").replace(",", "").split(' ')

    for word in word_chunk:
        if target_word == word:
            return True

    return False


def transcribe_buffer(audio_buffer, model):
    # TODO find out why this doesn't like being called
    # segments, _ = model.transcribe(audio_buffer)
    result_text = "nope nope nope "

    # for segment in segments:
    #    result_text += segment.text

    return result_text


def transcription_handler(name, q_transcriber, q_volume_control):

    print("Transcriber Process started - Initializing model...")
    model = WhisperModel("tiny", device="cpu", compute_type="int8")
    print("Model initialized!")

    # TODO Use a timestamp to mark when the SAM was active, maybe after 10 secs of non-detection, turn SAM back off

    while True:
        try:

            audio_buffer_copy = q_transcriber.get()

            transcription = transcribe_buffer(audio_buffer_copy, model)

            print(transcription)

            if (word_check(transcription, name)):
                q_volume_control.put(VolumeCommand(0.5, 0.5))

        except KeyboardInterrupt:
            print("Transcriber Process stopped")
            break
                   
