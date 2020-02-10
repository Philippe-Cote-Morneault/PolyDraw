import urllib.request
import json
import random
import string
import socket
import binascii
import threading
import msgpack
import uuid
import importlib
import commands
from termcolor import colored




client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

def randomString(stringLength=10):
    """Generate a random string of fixed length """
    letters = string.ascii_lowercase
    return ''.join(random.choice(letters) for i in range(stringLength))

def authREST():
    body = {'username':'dummy-'+randomString(6)}
    myurl = "http://localhost:3000/auth"
    req = urllib.request.Request(myurl)
    req.add_header('Content-Type', 'application/json; charset=utf-8')
    jsondata = json.dumps(body)
    jsondataasbytes = jsondata.encode('utf-8')   # needs to be bytes
    req.add_header('Content-Length', len(jsondataasbytes))
    response = urllib.request.urlopen(req, jsondataasbytes)
    if response.code == 200:
        print("REST - 200 /auth")
        jsonresponse = json.load(response)
        print(jsonresponse)
        return jsonresponse["SessionToken"]
    else:
        print("REST - {} /auth".format(response.code))

def sendMessage(typeVal, bytesArray):
    if bytesArray is None:
        payload = bytearray(3)
        payload[1:3] = [0x00,0x00]
    else:
        payload = bytearray(3 + len(bytesArray))
        payload[1:3] = len(bytesArray).to_bytes(2, byteorder='big')
        payload[3:] = bytesArray
    payload[0] = typeVal
    print(colored("Sending:","magenta"), "{}".format(str(payload.hex())))
    print()
    client.send(payload)

def socketReceive():
    while True:
        if client._closed:
            print("Socket closed!")
            return
        tl_part = client.recv(3)
        if(len(tl_part) == 3):
            # We have the tl part
            typeVal = tl_part[0]
            sizeVal = int.from_bytes(tl_part[1:3], byteorder='big')
            if sizeVal > 0:
                val = client.recv(sizeVal)
                if(len(val) == sizeVal):
                    print(colored("Received:","green"), "{} | {}".format(str(typeVal), str(val.hex())))
                    handle(typeVal, val)
                else:
                    print("ERROR: SOCKET Missing message part value")
            else:
                print(colored("Received:","green"), "{} | {}".format(str(typeVal), "''"))
                handle(typeVal, bytearray(0))

def printType(str):
    print(colored(str, 'green'))
def printMsgPack(msg):
     print(colored(msg, 'yellow'))

def handle(typeVal, valBytes):
    if typeVal == 9:
        printType("-> Healthcheck")
        sendMessage(10, None)
    if typeVal == 21:
        printType("-> Message received")
        printMsgPack(msgpack.unpackb(valBytes))
    if typeVal == 23:
        printType("-> Channel join")
        printMsgPack(msgpack.unpackb(valBytes))
    if typeVal == 25:
        printType("-> Channel quit")
        printMsgPack(msgpack.unpackb(valBytes))
    if typeVal == 27:
        printType("-> Channel create")
        printMsgPack(msgpack.unpackb(valBytes))
    if typeVal == 29:
        printType("-> Channel create")
        printMsgPack(msgpack.unpackb(valBytes))

def main():
    sessionToken = str(authREST())
    client.connect(('127.0.0.1', 3001))
    client.setblocking(True)
    
    commands.load(client)
    
    receiver = threading.Thread(target=socketReceive)
    receiver.start()
    
    # Authentification
    sendMessage(0,sessionToken.encode("utf-8"))

    sendMessage(20, msgpack.packb(
        {"Message":"Hello script test", "ChannelID":str(uuid.uuid4())}
    ))

    while True:
        input()
        importlib.reload(commands)
        commands.load(sendMessage)
        commands.execute()
    receiver.join()
    client.close()
    
main()