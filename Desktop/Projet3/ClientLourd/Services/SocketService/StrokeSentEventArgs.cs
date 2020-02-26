using ClientLourd.Models.Bindable;
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;

namespace ClientLourd.Services.SocketService
{
    class StrokeSentEventArgs: EventArgs
    {
        private StrokeInfo _strokeInfo;

        public StrokeSentEventArgs(dynamic data)
        {
            _strokeInfo = new StrokeInfo(data);

        }

        public StrokeInfo StrokeInfo
        {
            get => _strokeInfo;
        }
    }
}
