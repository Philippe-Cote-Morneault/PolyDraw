using System;
using System.Text;
using ClientLourd.Utilities.Enums;
using MessagePack;
using MessagePack.Resolvers;

namespace ClientLourd.Models

{
    public class TLV
    {
        public TLV(SocketMessageTypes type)
        {
            Type = (byte) ((int) type);
        }
        
        /// <summary>
        /// Serialize the message using message pack
        /// </summary>
        /// <param name="type"></param>
        /// <param name="message"></param>
        public TLV(SocketMessageTypes type, dynamic message)
        {
            Type = (byte) ((int) type);
            Value = MessagePackSerializer.Serialize(message, ContractlessStandardResolver.Options);
        }
        
        /// <summary>
        /// Convert the message in byte 
        /// </summary>
        /// <param name="type"></param>
        /// <param name="message"></param>
        public TLV(SocketMessageTypes type, string message)
        {
            Type = (byte) ((int) type);
            Value = Encoding.ASCII.GetBytes(message);
        }

        public byte[] GetBytes()
        {
            byte[] bytes = new Byte[1 + 2 + (Value !=null ? Value.Length : 0)];
            bytes[0] = Type;

            byte[] lengthInBytes = BitConverter.GetBytes(Length);

            // Convert to big-endian
            Array.Reverse(lengthInBytes);
        
            lengthInBytes.CopyTo(bytes, 1);
            Value?.CopyTo(bytes, 3);

            return bytes;
        }

        public byte Type { get; private set; }

        public UInt16 Length
        {
            get
            {
                if (Value != null)
                {
                    return (UInt16) Value.Length;
                }
                return 0;
            }
        }
        public byte[] Value { get; private set; }


        
    }
}