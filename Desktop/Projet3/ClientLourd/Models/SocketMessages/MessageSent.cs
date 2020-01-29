using MessagePack;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ClientLourd.Models.SocketMessages
{
    [MessagePackObject]
    public class MessageSent
    {
        [Key(0)]
        public string message { get; set; }

        [Key(1)]
        public string canalID { get; set; }
    }
}
