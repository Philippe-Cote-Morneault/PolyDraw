using System;
using System.IO;
using System.Runtime.Serialization.Formatters.Binary;
using System.Text;
using ClientLourd.Utilities.Enums;
using MessagePack;
using ClientLourd.Models.SocketMessages;

namespace ClientLourd.Models

{
    public class TLV
    {
        public TLV(SocketMessageTypes type, string message, string canalID)
        {
            //TODO Change this
            Type = (byte) ((int) type);
            Value = Encoding.ASCII.GetBytes(message);
            MessageSent ms = new MessageSent() { message = message, canalID = canalID };
            Value = MessagePackSerializer.Serialize(ms);
            Length = (UInt16) Value.Length;
        }

        public TLV(byte type, UInt16 length, byte[] value)
        {
            Type = type;
            Length = length;
            Value = value;
        }


        public byte[] GetBytes()
        {
            byte[] bytes = new Byte[1 + 2 + Value.Length];
            bytes[0] = Type;

            byte[] lengthInBytes = BitConverter.GetBytes(Length);

            // Convert to big-endian
            Array.Reverse(lengthInBytes);
        
            lengthInBytes.CopyTo(bytes, 1);
            Value.CopyTo(bytes, 3);

            return bytes;
        }

        public byte Type { get; set; }
        public UInt16 Length { get; set; }
        public byte[] Value { get; set; }


        
    }
}