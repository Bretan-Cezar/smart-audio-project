import numpy as np
from jack import Client, OwnPort
import json
from threading import Event
from shutil import get_terminal_size

config: dict
SR: int
BLOCK_SIZE: int
GAIN_MEDIA: float
GAIN_MIC: float

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
    GAIN_MEDIA = config["mediaInputGain"]
    GAIN_MIC = config["micInputGain"]

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

    def process(frames: int):
        # assert len(client.inports) == len(client.outports)
        # assert frames == BLOCK_SIZE
        
        buf_micL = np.array(micL.get_array(), dtype=np.float32)
        buf_micR = np.array(micR.get_array(), dtype=np.float32)
        buf_mediaL = np.array(mediaL.get_array(), dtype=np.float32)
        buf_mediaR = np.array(mediaR.get_array(), dtype=np.float32)

        buf_mixL = (GAIN_MEDIA * buf_mediaL) + (GAIN_MIC * buf_micL)
        buf_mixR = (GAIN_MEDIA * buf_mediaR) + (GAIN_MIC * buf_micR)

        outL.get_array()[:] = buf_mixL
        outR.get_array()[:] = buf_mixR


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

        try:
            event.wait()
        except KeyboardInterrupt:
            print("\nUser Interrupt")

