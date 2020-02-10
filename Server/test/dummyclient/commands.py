import msgpack
import uuid

sendMessage = None

def load(funcSendMessage):
    global sendMessage
    sendMessage = funcSendMessage

def execute():
    # You can add anything to reload
    sendMessage(20, msgpack.packb(
        {"Message":"Hello ca va", "ChannelID":str(uuid.uuid4())}
    ))
