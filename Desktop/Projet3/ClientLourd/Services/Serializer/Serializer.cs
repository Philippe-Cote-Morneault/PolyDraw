using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.Serialization.Formatters.Binary;
using System.Text;
using System.Threading.Tasks;

namespace ClientLourd.Services.Serializer
{
    class Serializer
    {
        static public byte[] ToByteArray(object obj)
        { 
            using (var memoryStream = new MemoryStream())
            {
                (new BinaryFormatter()).Serialize(memoryStream, obj);
                return memoryStream.ToArray();
            }
        }

        static public object Deserialize(byte[] obj)
        {
            using (var memoryStream = new MemoryStream(obj))
                return (new BinaryFormatter()).Deserialize(memoryStream);
        }
    }
}
