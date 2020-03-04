using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ClientLourd.Services.SocketService
{
    public class LobbyCreatedArgs: EventArgs
    {
        private dynamic _data;
        public LobbyCreatedArgs(dynamic data)
        {
            _data = data;
        }

        public string ID { get => _data["ID"]; }

        public string Name { get => _data["Name"]; }

        public string OwnerName { get => _data["OwnerName"]; }

        public string OwnerID { get => _data["OwnerID"]; }

        public int PlayersMax { get => (int)_data["PlayersMax"]; }

        public int Mode{ get => (int)_data["Mode"]; }
    }
}
