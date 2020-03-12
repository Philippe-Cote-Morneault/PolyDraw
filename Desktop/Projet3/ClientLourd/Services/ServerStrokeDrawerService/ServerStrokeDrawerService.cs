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
        // Used to know when all the strokes sent from the server were drawn
        public delegate void PreviewDrawingDoneHandler(object source, EventArgs args);
        public event PreviewDrawingDoneHandler PreviewDrawingDoneEvent;
        private int _messageDequeuedCounter;
        public int TotalMessagesSent { get; set; }

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
            _drawTimer.Stop();


            if (IsPreview)
            {
                TotalMessagesSent = -1;
                _messageDequeuedCounter = 0;
            }
        }

        public void Start()
        {
            if (!_drawTimer.Enabled)
            {
                _drawTimer.Start();
            }
        }

        public void Stop()
        {
            if (_drawTimer.Enabled)
            {
                _drawTimer.Stop();
            }
        }

        private void Draw(object source, EventArgs args)
        {
            Stop();

            if (!_strokeInfoQueue.IsEmpty)
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
                            _messageDequeuedCounter++;
                        }
                        else
                        { 
                            _canvas.AddStroke(strokeInfo);
                        }
                    }
                });

            }
            if (_isPreview && AllStrokesWereDrawn())
            {
                PreviewDrawingDoneEvent.Invoke(source, EventArgs.Empty);
                ResetPreviewCounters();
                return;
            }
            Start();
        }

        private void ResetPreviewCounters()
        {
            TotalMessagesSent = -1;
            _messageDequeuedCounter = 0;
        }

        private bool AllStrokesWereDrawn()
        {
            return (TotalMessagesSent != -1 && _messageDequeuedCounter == TotalMessagesSent);
        }

        public void Enqueue(StrokeInfo strokeInfo)
        {
            _strokeInfoQueue.Enqueue(strokeInfo);
        }
    }
}
