using System;
using System.IO;
using System.Runtime.Serialization.Formatters.Binary;
using System.Text;
using ClientLourd.Utilities.Enums;
using ClientLourd.Services.Serializer;

namespace ClientLourd.Models
{
    [Serializable()]
    public class TLV
    {
        public TLV(SocketMessageTypes type, string message)
        {
            //TODO
            Type = (byte) ((int) type);
            Value = Encoding.ASCII.GetBytes(message);
            Length = (UInt16) Value.Length;
        }

        public TLV(SocketMessageTypes type, object value)
        {
            Type = (byte)((int)type);
            Value = Serializer.ToByteArray(value);
            Length = (UInt16)Value.Length;
        }

        public TLV(byte type, UInt16 length, byte[] value)
        {
            Type = type;
            Length = length;
            Value = value;
        }


        public byte Type { get; set; }
        public UInt16 Length { get; set; }
        public byte[] Value { get; set; }


        
    }
}