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
        private bool _isPreview;

        public ServerStrokeDrawerService(System.Windows.Controls.InkCanvas canvas, bool IsPreview)
        {
            _canvas = canvas;
            _isPreview = IsPreview;
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
                    if (strokeInfo != null && strokeInfo.PointCollection.Count > 0)
                    {
                        if (_isPreview)
                        {
                            _canvas.AddStrokePreview(strokeInfo);
                        }
                        else
                        { 
                            _canvas.AddStroke(strokeInfo);
                        }
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
