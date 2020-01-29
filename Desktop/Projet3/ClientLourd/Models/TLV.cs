using System;
using System.IO;
using System.Runtime.Serialization.Formatters.Binary;
using System.Text;
using ClientLourd.Utilities.Enums;
using MessagePack;
using ClientLourd.Models.SocketMessages;
using MessagePack.Resolvers;

namespace ClientLourd.Models

{
    public class TLV
    {
        public TLV(SocketMessageTypes type, dynamic message)
        {
            Type = (byte) ((int) type);
            Value = MessagePackSerializer.Serialize(message, ContractlessStandardResolver.Options);
        }

        public TLV(byte type, UInt16 length, byte[] value)
        {
            Type = type;
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

        public byte Type { get; private set; }

        public UInt16 Length
        {
            get { return (UInt16) Value.Length; }
        }
        public byte[] Value { get; private set; }


        
    }
}