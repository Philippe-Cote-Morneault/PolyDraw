using System.IO;
using System.Net;
using ClientLourd.Models.Bindable;

namespace ClientLourd.Models.NonBindable
{
    public class NetworkInformations
    {
        //public const string HOST_NAME = "log3900.fsae.polymtl.ca";
        public const string HOST_NAME = "log3900-203.canadacentral.cloudapp.azure.com";
        public const int DEV_SOCKET_PORT = 5011;
        public const int DEV_REST_PORT = 5010;
        public const int PROD_SOCKET_PORT = 5001;
        public const int PROD_REST_PORT = 5000;
        public const int LOCAL_SOCKET_PORT = 3001;
        public const int LOCAL_REST_PORT = 3000;

        public NetworkInformations()
        {
            //prod as default
            Config = 0;
        }

        public int Config { get; set; }

        public IPAddress IP
        {
            get
            {
                switch (Config)
                {
                    case 0:
                    case 1:
                        return Dns.GetHostAddresses(HOST_NAME)[0];
                    case 2:
                        return IPAddress.Parse("127.0.0.1");

                    default:
                        throw new InvalidDataException("Invalid network configuration");
                }
            }
        }

        public int RestPort
        {
            get
            {
                switch (Config)
                {
                    case 0:
                        return PROD_REST_PORT;
                    case 1:
                        return DEV_REST_PORT;
                    case 2:
                        return LOCAL_REST_PORT;
                    default:
                        throw new InvalidDataException("Invalid network configuration");
                }
            }
        }

        public int SocketPort
        {
            get
            {
                switch (Config)
                {
                    case 0:
                        return PROD_SOCKET_PORT;
                    case 1:
                        return DEV_SOCKET_PORT;
                    case 2:
                        return LOCAL_SOCKET_PORT;
                    default:
                        throw new InvalidDataException("Invalid network configuration");
                }
            }
        }
    }
}