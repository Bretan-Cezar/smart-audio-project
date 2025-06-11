#!/usr/bin/python

# File: bleServer.py
# Auth: P Srinivas Rao
# Desc: Bluetooth server application that uses RFCOMM sockets

import os
import sys
import time
import logging
import logging.config
import json # Uses JSON package
import pickle # Serializing and de-serializing a Python object structure
import socket

logger = logging.getLogger('bleServerLogger')

def startLogging(
    default_path='configLogger.json',
    default_level=logging.INFO,
    env_key='LOG_CFG'
):
    # Setup logging configuration
    path = default_path
    value = os.getenv(env_key, None)
    if value:
        path = value

    if os.path.exists(path):
        with open(path, 'rt') as f:
            config = json.load(f)

        logging.config.dictConfig(config)

    else:
        logging.basicConfig(level=default_level)

class BleServer:

    def __init__(self):

        self.serverSocket: socket.socket
        self.clientSocket: socket.socket
        self.serviceName = "BluetoothService"
        self.jsonFile = "text.json"
        self.uuid = "00001101-0000-1000-8000-00805F9B34FB"

    def getBluetoothSocket(self):
        try:
            self.serverSocket = socket.socket(socket.AF_BLUETOOTH, socket.SOCK_STREAM, socket.BTPROTO_RFCOMM)          
            logger.info("Bluetooth server socket successfully created for RFCOMM service...")

        except (SystemExit, KeyboardInterrupt) as e:
            logger.error("Failed to create the bluetooth server socket ", exc_info=True)

    def getBluetoothConnection(self):
        try:
            self.serverSocket.bind(("DC:A6:32:70:23:BB", 57))
            logger.info("Bluetooth server socket bind successfully on host "" to port 57...")

        except (Exception, SystemExit, KeyboardInterrupt) as e:
            logger.error("Failed to bind server socket on host to port 57 ... ", exc_info=True)

        try:
            self.serverSocket.listen(1)
            logger.info("Bluetooth server socket put to listening mode successfully ...")

        except (Exception, SystemExit, KeyboardInterrupt) as e:
            logger.error("Failed to put server socket to listening mode  ... ", exc_info=True)

        try:
            port = self.serverSocket.getsockname()[1]
            logger.info("Waiting for connection on RFCOMM channel %d" % (port))

        except (Exception, SystemExit, KeyboardInterrupt) as e:
            logger.error("Failed to get connection on RFCOMM channel  ... ", exc_info=True)


   # def advertiseBluetoothService(self):
   #     try:
   #         advertise_service( 
   #             self.serverSocket, 
   #             self.serviceName,
   #             service_id = self.uuid,
   #             service_classes = [ self.uuid, SERIAL_PORT_CLASS ],
   #             profiles = [ SERIAL_PORT_PROFILE ],
   #     #       protocols = [ OBEX_UUID ]
   #         )
   #         logger.info("%s advertised successfully ..." % (self.serviceName))

   #     except (Exception, SystemExit, KeyboardInterrupt) as e:
   #         logger.error("Failed to advertise bluetooth services  ... ", exc_info=True)

    def acceptBluetoothConnection(self):
        try:
            self.clientSocket, clientInfo = self.serverSocket.accept()
            logger.info("Accepted bluetooth connection from %s", clientInfo)

        except (Exception, SystemExit, KeyboardInterrupt) as e:
            logger.error("Failed to accept bluetooth connection ... ", exc_info=True)

    def recvData(self):
        try:
            while True:
                data = self.serverSocket.recv(1024)

                if not data:
                    # self.clientSocket.send("EmptyBufferResend")
                    logger.warn("Empty buffer received")

                # remove the length bytes from the front of buffer
                # leave any remaining bytes in the buffer!
                dataSizeStr, ignored, data = data.partition(b':')
                dataSize = int(dataSizeStr)

                if len(data) < dataSize:
                    logger.error("Corrupted buffer received")
                else:
                    break

            logger.info("Data received successfully over bluetooth connection")
            return data

        except (Exception, IOError) as e:
            pass

    def deserializedData(self, _dataRecv):
        try:

            self.dataObj = json.loads(_dataRecv)
            logger.info("Serialized string converted successfully to object")

        except (Exception) as e:
            logger.error("Failed to deserialize string ... ", exc_info=True)

    def writeJsonFile(self):
        try:
            # Open a file for writing
            jsonFileObj = open(self.jsonFile, "w+")
            logger.info("%s file successfully opened to %s" % (self.jsonFile, jsonFileObj))
            # Save the dictionary into this file
            # (the 'indent=4' is optional, but makes it more readable)
            json.dump(self.dataObj,jsonFileObj, indent=4)
            logger.info("Content dumped successfully to the %s file" %(self.jsonFile))
            # Close the file
            jsonFileObj.close()
            logger.info("%s file successfully closed" %(self.jsonFile))

        except (Exception, IOError) as e:
            logger.error("Failed to write json contents to the file ... ", exc_info=True)

    def closeBluetoothSocket(self):
        try:
            self.clientSocket.close()
            self.serverSocket.close()
            logger.info("Bluetooth sockets successfully closed ...")

        except (Exception) as e:
            logger.error("Failed to close the bluetooth sockets ", exc_info=True)

    def start(self):
            # Create the server socket
            self.getBluetoothSocket()
            # get bluetooth connection to port # of the first available
            self.getBluetoothConnection()
            # advertising bluetooth services
            # self.advertiseBluetoothService()
            # Accepting bluetooth connection
            self.acceptBluetoothConnection()

    def receive(self):
            # receive data
            dataRecv=self.recvData()
            # de-serializing data
            self.deserializedData(dataRecv)
            # Writing json object to the file
            self.writeJsonFile()

    def stop(self):
            # Disconnecting bluetooth sockets
            self.closeBluetoothSocket()

if __name__ == '__main__':

    startLogging()

    bleSvr = BleServer()
    bleSvr.start()
    bleSvr.receive()
    bleSvr.stop()
