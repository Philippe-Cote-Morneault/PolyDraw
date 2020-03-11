using ClientLourd.Models.NonBindable;
using ClientLourd.Utilities.Extensions;
using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;



namespace ClientLourd.Services.ServerStrokeDrawerService
{
    public class ServerStrokeDrawerService
    {
        private ConcurrentQueue<StrokeInfo> _strokeInfoQueue;
        private System.Timers.Timer _drawTimer;
        private System.Windows.Controls.InkCanvas _canvas;

        public ServerStrokeDrawerService(System.Windows.Controls.InkCanvas canvas)
        {
            _canvas = canvas;
            _strokeInfoQueue = new ConcurrentQueue<StrokeInfo>();
            _drawTimer = new System.Timers.Timer(5);
            _drawTimer.Elapsed += Draw;
            _drawTimer.Start();
        }


        private void Draw(object source, EventArgs args)
        {
            _drawTimer.Stop();
            if (_strokeInfoQueue.Count != 0)
            {

                Application.Current.Dispatcher.Invoke(delegate
                {
                    StrokeInfo strokeInfo;
                    _strokeInfoQueue.TryDequeue(out strokeInfo);
                    if (strokeInfo != null)
                    {
                        _canvas.AddStrokePreview(strokeInfo);
                    }
                });

            }
            _drawTimer.Start();
        }

        public void Enqueue(StrokeInfo strokeInfo)
        {
            _strokeInfoQueue.Enqueue(strokeInfo);
        }
    }
}
