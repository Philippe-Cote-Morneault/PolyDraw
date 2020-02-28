using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ClientLourd.Services.SocketService
{
    public class DrawingEventArgs: EventArgs
    {
        public DrawingEventArgs(dynamic data)
        {
            _data = data;
        }

        private dynamic _data;

        public dynamic Data
        {
            get => _data;
        }

    }
}
