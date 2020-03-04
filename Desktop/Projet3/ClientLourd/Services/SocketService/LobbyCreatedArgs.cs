using ClientLourd.Models.Bindable;
using Newtonsoft.Json;
using RestSharp.Serialization.Json;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
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
            Players = new ObservableCollection<Player>();

            
            for (int i = 0; i < ((dynamic[])_data["Players"]).Length; i++)
            {
                Players.Add(new Player(_data["Players"][i]["IsCPU"], _data["Players"][i]["ID"], _data["Players"][i]["Username"]));
            }
        }

        public string ID { get => _data["ID"]; }

        public string Name { get => _data["Name"]; }

        public string OwnerName { get => _data["OwnerName"]; }

        public string OwnerID { get => _data["OwnerID"]; }

        public int PlayersMax { get => (int)_data["PlayersMax"]; }

        public int Mode{ get => (int)_data["Mode"]; }

        public int Difficulty { get => (int)_data["Difficulty"]; }

        private ObservableCollection<Player> _players;
        public ObservableCollection<Player> Players //{ get => JsonConvert.DeserializeObject<Player>(_data["Players"][0]); }
        {
            get
            {

                return _players;
            }
            set
            {
                _players = value;
            }
        }
    }

    
}
