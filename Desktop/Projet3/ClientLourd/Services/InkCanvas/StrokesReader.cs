using System;
using System.Collections.Generic;
using System.Threading;
using System.Timers;
using System.Windows;
using System.Windows.Input;
using ClientLourd.Services.SocketService;
using System.Windows.Controls;
using System.Windows.Ink;
using ClientLourd.Views.Controls;
using Timer = System.Timers.Timer;

namespace ClientLourd.Services.InkCanvas
{
    public class StrokesReader
    {
        private const int SEND_RATE = 20;
        private Editor _editor;
        private List<Point> _points;
        private Timer _timer;
        private SocketClient _socket;
        private byte[] _strokeID;
        private Mutex _mutex;
        
        public StrokesReader(Editor editor, SocketClient socket)
        {
            _mutex = new Mutex();
            _editor = editor;
            _socket = socket;
            _editor.surfaceDessin.AddHandler(UIElement.MouseDownEvent, new MouseButtonEventHandler(CanvasOnMouseDown), true);
            _points = new List<Point>();
            _timer = new Timer(20);
            _timer.Elapsed += TimerOnElapsed;
        }

        private void TimerOnElapsed(object sender, ElapsedEventArgs e)
        {
            _mutex.WaitOne();
            //Send all the points
            if(_strokeID != null)
            {
                Console.WriteLine(_points.Count);
                System.Diagnostics.Debug.WriteLine(_points.Count);
            }
            _points.Clear();
            _mutex.ReleaseMutex();
        }

        public void Start()
        {
            //TODO send message to server
            _editor.surfaceDessin.MouseMove += CanvasOnMouseMove;
            _editor.surfaceDessin.MouseDown += CanvasOnMouseDown;
            _editor.surfaceDessin.MouseUp += CanvasOnMouseUp;
            _timer.Start();
        }
        public void Stop()
        {
            //TODO send message to server
            _timer.Stop();
            _editor.surfaceDessin.MouseMove -= CanvasOnMouseMove;
            _editor.surfaceDessin.MouseDown -= CanvasOnMouseDown;
            _editor.surfaceDessin.MouseUp -= CanvasOnMouseUp;
        }


        private void CanvasOnMouseUp(object sender, MouseButtonEventArgs e)
        {
            _strokeID = null;
        }

        private void CanvasOnMouseDown(object sender, MouseButtonEventArgs e)
        {
            _mutex.WaitOne();
            _points.Clear();
            _strokeID = new Guid().ToByteArray();
            _mutex.ReleaseMutex();
        }

        private void CanvasOnMouseMove(object sender, MouseEventArgs e)
        {
            if (_strokeID != null)
            {
                _points.Add(e.GetPosition(_editor.surfaceDessin));
            }
        }
    }
}