import socket
import json
from time import sleep
import sys

def get_bluetooth_socket() -> socket.socket | None:
    server_socket: socket.socket | None

    try:
        server_socket = socket.socket(socket.AF_BLUETOOTH, socket.SOCK_STREAM, socket.BTPROTO_RFCOMM)          
        print("Bluetooth server socket successfully created for RFCOMM service")

    except (SystemExit, KeyboardInterrupt) as e:
        server_socket = None
        print(f"Failed to create the bluetooth server socket: {e.with_traceback(None)}")

    return server_socket


def listen_for_bluetooth_connection(server_socket: socket.socket, channel: int):

    try:
        server_socket.bind(("DC:A6:32:70:23:BB", channel))
        print("Bluetooth server socket bind successfully")

    except (Exception, SystemExit, KeyboardInterrupt) as e:
        print(f"Failed to bind server socket: {e.with_traceback(None)}")

    try:
        server_socket.listen(1)
        print("Bluetooth server socket put to listening mode successfully ...")

    except (Exception, SystemExit, KeyboardInterrupt) as e:
        print(f"Failed to put server socket to listening mode: {e.with_traceback(None)}")

    try:
        channel = server_socket.getsockname()[1]
        print("Waiting for connection on RFCOMM channel %d" % (channel))

    except (Exception, SystemExit, KeyboardInterrupt) as e:
        print(f"Failed to get connection on RFCOMM channel: {e.with_traceback(None)}")


def accept_bluetooth_connection(server_socket: socket.socket) -> socket.socket | None:
    new_socket: socket.socket | None

    try:

        new_socket, client_info = server_socket.accept()
        print("Accepted bluetooth connection from %s", client_info)

    except (Exception, SystemExit, KeyboardInterrupt) as e:

        new_socket = None
        print(f"Failed to accept bluetooth connection: {e.with_traceback(None)}")

    return new_socket


def recv_data(server_socket: socket.socket) -> bytes | None:
    try:
        data = server_socket.recv(4096)

        if not data:
            print("W: Empty Buffer Received")

        # Trim NULL terminators
        data, _, _ = data.partition(b'\x00')

        print(f"{str(len(data))} bytes received over bluetooth connection")

        return data

    except (Exception, IOError) as e:

        print(f"Exception encountered on recv: {e.with_traceback(None)}")
        return None


def send_data(server_socket: socket.socket, data: bytes):
    sent_size: int = 0

    try:

        while True:
            sent_size += server_socket.send(data)

            if sent_size < len(data):
                print("E: Buffer partially sent")

            else:
                break

        print(f"{sent_size} bytes sent successfully over bluetooth connection")

    except (Exception, IOError) as e:
        print(f"Exception encountered on send: {e.with_traceback(None)}")


def socket_server(GAIN_MEDIA, GAIN_MIC, RELOAD_SIGNAL):

    print("Socket Server started.")

    with open("config.json", "rt") as cfg:
        config = json.load(cfg)

    server_socket: socket.socket | None = get_bluetooth_socket()

    if server_socket != None:
        listen_for_bluetooth_connection(server_socket, int(config["bluetoothSocketChannel"]))

        server_socket = accept_bluetooth_connection(server_socket)

        if server_socket != None:
            current_modifiable_settings = {
                "smartAmbienceMode": str(config["smartAmbienceMode"]),
                "micInputGainStateEnabled": float(config["micInputGainStateEnabled"]),
                "phraseList": list(config["phraseList"])
            }

            data = bytes(json.dumps(current_modifiable_settings), encoding='utf-8')
            
            print(len(data))

            send_data(server_socket, data)

            while True:

                try:

                    data = recv_data(server_socket)

                    if data != None:
                        
                        modified_settings: dict = json.loads(str(data, encoding='utf-8'))
                        
                        for k, v in modified_settings.items():
                            config[k] = v

                        with open("config.json", "wt") as cfg:
                            json.dump(config, cfg, indent=4)
                                        
                        RELOAD_SIGNAL.value = True

                        sleep(5)

                        RELOAD_SIGNAL.value = False

                except KeyboardInterrupt:

                    server_socket.close()

                    print("Socket Server stopped")
                    sys.exit(15)



