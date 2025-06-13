import numpy as np
from jack import Client, OwnPort
import json
from threading import Event
from shutil import get_terminal_size
from bt_socket_server import socket_server
from vol_ctrl import volume_handler
from transcriber import transcription_handler
from audio_chunk_handler import chunk_handler
from multiprocessing import Value, Queue, Process
from queue import ShutDown
from typing import Any
import ctypes
import sys
from time import sleep

config: dict
SR: int
BLOCK_SIZE: int
GAIN_MEDIA: Any
GAIN_MIC: Any

if __name__ == "__main__":
    
    with open("config.json", "rt") as cfg:
        config = json.load(cfg)

    event = Event()
    client = Client(config["clientName"])

    def shutdown(status, reason):
        print(f"JACK shutdown!\nstatus:{status}\nreason:{reason}")
        event.set()

    client.set_shutdown_callback(shutdown)
    
    L_MEDIA_PORT = config["leftChannelMediaPort"]
    R_MEDIA_PORT = config["rightChannelMediaPort"]
    L_MIC_PORT = config["leftChannelMicPort"]
    R_MIC_PORT = config["rightChannelMicPort"]
    L_OUTPORT = config["leftChannelOutPort"]
    R_OUTPORT = config["rightChannelOutPort"]
    GAIN_MEDIA = Value(ctypes.c_float, float(config["mediaInputGainStateDisabled"]), lock=True)
    GAIN_MIC = Value(ctypes.c_float, float(config["micInputGainStateDisabled"]), lock=True)

    inport_pairs = []

    server_inport = client.get_port_by_name(L_MEDIA_PORT)
    client_inport = client.inports.register(L_MEDIA_PORT)
    inport_pairs.append((server_inport, client_inport))

    server_inport = client.get_port_by_name(L_MIC_PORT)
    client_inport = client.inports.register(L_MIC_PORT)
    inport_pairs.append((server_inport, client_inport))

    server_inport = client.get_port_by_name(R_MEDIA_PORT)
    client_inport = client.inports.register(R_MEDIA_PORT)
    inport_pairs.append((server_inport, client_inport))

    server_inport = client.get_port_by_name(R_MIC_PORT)
    client_inport = client.inports.register(R_MIC_PORT)
    inport_pairs.append((server_inport, client_inport))

    outport_pairs = []

    server_outport = client.get_port_by_name(L_OUTPORT)
    client_outport = client.outports.register(L_OUTPORT)
    outport_pairs.append((client_outport, server_outport))

    server_outport = client.get_port_by_name(R_OUTPORT)
    client_outport = client.outports.register(R_OUTPORT)
    outport_pairs.append((client_outport, server_outport))
    
    SR = client.samplerate
    BLOCK_SIZE = client.blocksize

    mediaL: OwnPort
    mediaR: OwnPort
                   
    micL: OwnPort
    micR: OwnPort
                   
    outL: OwnPort
    outR: OwnPort

    q_audio_chunks = Queue()
    q_transcriber = Queue()
    q_volume_control = Queue()
    
    def process(frames: int):

        buf_micL = np.array(micL.get_array(), dtype=np.float32)
        buf_micR = np.array(micR.get_array(), dtype=np.float32)
        buf_mediaL = np.array(mediaL.get_array(), dtype=np.float32)
        buf_mediaR = np.array(mediaR.get_array(), dtype=np.float32)

        buf_mixL = (GAIN_MEDIA.value * buf_mediaL) + (GAIN_MIC.value * buf_micL)
        buf_mixR = (GAIN_MEDIA.value * buf_mediaR) + (GAIN_MIC.value * buf_micR)

        outL.get_array()[:] = buf_mixL
        outR.get_array()[:] = buf_mixR

        if frames == BLOCK_SIZE:

            try:
                q_audio_chunks.put(
                    np.stack((buf_micL, buf_micR))
                )
            except ShutDown:
                pass

    client.set_process_callback(process)

    mediaL = client.get_port_by_name(f"{client.name}:{L_MEDIA_PORT}")
    mediaR = client.get_port_by_name(f"{client.name}:{R_MEDIA_PORT}")

    micL = client.get_port_by_name(f"{client.name}:{L_MIC_PORT}")
    micR = client.get_port_by_name(f"{client.name}:{R_MIC_PORT}")
    outL = client.get_port_by_name(f"{client.name}:{L_OUTPORT}")
    outR = client.get_port_by_name(f"{client.name}:{R_OUTPORT}")

    with client:
        
        for (s, c) in inport_pairs:
            client.connect(s, c)

        for (c, s) in outport_pairs:
            client.connect(c, s)

        print("JACK CLIENT STARTED, ctrl+c TO QUIT".center(get_terminal_size().columns, "="))

        p1 = Process(target=chunk_handler, args=(BLOCK_SIZE, q_audio_chunks, q_transcriber, q_volume_control))
        p2 = Process(target=transcription_handler, args=(q_transcriber, q_volume_control))
        p3 = Process(target=volume_handler, args=(GAIN_MIC, GAIN_MEDIA, q_volume_control))
        p4 = Process(target=socket_server, args=(GAIN_MEDIA, GAIN_MIC))

        try:
            p1.start()
            p2.start()
            p3.start()
            p4.start()

            while True:

                p1.join()
                print("Audio Chunk Handler process sucessfully exited with code 0")
                p2.join()
                print("Transcription Handler process sucessfully exited with code 0")
                p3.join()
                print("Volume Handler process sucessfully exited with code 0")
                
                print("All Handler processes stopped due to config reload, restarting...")

                q_audio_chunks = Queue()
                q_transcriber = Queue()
                q_volume_control = Queue()

                p1 = Process(target=chunk_handler, args=(BLOCK_SIZE, q_audio_chunks, q_transcriber, q_volume_control, RELOAD_SIGNAL))
                p2 = Process(target=transcription_handler, args=(q_transcriber, q_volume_control, RELOAD_SIGNAL))
                p3 = Process(target=volume_handler, args=(GAIN_MIC, GAIN_MEDIA, q_volume_control, RELOAD_SIGNAL))
                
                p1.start()
                p2.start()
                p3.start()

        except KeyboardInterrupt:
            p1.join()
            p2.join()
            p3.join()
            p4.join()

            print("\nDeactivating JACK Client...")

            client.deactivate()
            client.close()

            sys.exit(15)

