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
import argparse
from termcolor import colored




client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
settings = {
    "url":"localhost",
    "rest":3000,
    "socket":3001
}

def randomString(stringLength=10):
    """Generate a random string of fixed length """
    letters = string.ascii_lowercase
    return ''.join(random.choice(letters) for i in range(stringLength))

def setSettings(server):
    global settings        
    if server == "prod":
        settings = {
            "url":"log3900.fsae.polymtl.ca",
            "rest":5000,
            "socket":5001
        }
    elif server == "dev":
        settings = {
            "url":"log3900.fsae.polymtl.ca",
            "rest":5010,
            "socket":5011
        }
    print(settings)

def authREST(user):
    if user is None:
        body = {'username':'dummy-'+randomString(6)}
    else:
        body = {'username':user}
    myurl = "http://{}:{}/auth".format(settings["url"],settings["rest"])
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
        printType("-> Channel destroy")
        printMsgPack(msgpack.unpackb(valBytes))

def main():

    parser = argparse.ArgumentParser(prog='client', usage='python3 client.py [options]')
    parser.add_argument('--user', help='The username to use for the client')
    parser.add_argument('--server', choices=['prod','dev','local'], default='local', help="Which server should the client connect to")
    args = parser.parse_args()
    
    setSettings(args.server)
    sessionToken = str(authREST(args.user))
    client.connect((settings["url"], settings["socket"]))
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