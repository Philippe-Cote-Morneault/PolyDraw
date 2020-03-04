using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ClientLourd.Models.Bindable
{
    public class Player
    {
        public Player(bool isCPU, string id, string username)
        {
            IsCPU = isCPU;
            ID = id;
            Username = username;
        }

        public bool IsCPU { get; set; }
        public string ID { get; set; }
        public string Username { get; set; }
    }
}
