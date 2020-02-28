using System;
using System.Collections.Generic;
using System.Threading;
using System.Timers;
using System.Windows;
using System.Windows.Input;
using ClientLourd.Services.SocketService;
using System.Windows.Controls;
using System.Windows.Ink;
using System.Windows.Media;
using ClientLourd.Models.Bindable;
using ClientLourd.Models.NonBindable;
using ClientLourd.Utilities.Constants;
using ClientLourd.Utilities.Enums;
using ClientLourd.ViewModels;
using ClientLourd.Views.Controls;
using Point = System.Windows.Point;
using Timer = System.Timers.Timer;

namespace ClientLourd.Services.InkCanvas
{
    public class StrokesReader
    {
        private const int SEND_RATE = 20;
        private const int UUID_OFFSET = 17;
        private const int MAX_X_OFFSET = 33;
        private const int MAX_y_OFFSET = 35;
        private const int BRUSH_SIZE_OFFSET = 33;
        private const int POINTS_OFFSET = 34;
        private Editor _editor;
        private List<Point> _points;
        private Timer _timer;
        private SocketClient _socket;
        private Guid _currentStrokeID;
        private Mutex _mutex;

        private EditorInformation _information;
        
        public StrokesReader(Editor editor, SocketClient socket, EditorInformation information)
        {
            _information = information;
            _mutex = new Mutex();
            _editor = editor;
            _socket = socket;
            _editor.Canvas.AddHandler(UIElement.MouseDownEvent, new MouseButtonEventHandler(CanvasOnMouseDown), true);
            _points = new List<Point>();
            _timer = new Timer(SEND_RATE);
            _timer.Elapsed += TimerOnElapsed;
        }
        
        public void Start()
        {
            //TODO added the drawing id
            _socket.SendMessage(new Tlv(SocketMessageTypes.StartDrawing));
            _editor.Canvas.MouseMove += CanvasOnMouseMove;
            _editor.Canvas.MouseDown += CanvasOnMouseDown;
            _editor.StrokeDeleted += EditorOnStrokeDeleted;
            _editor.StrokedAdded += EditorOnStokeAdded;
            _timer.Start();
        }



        public void Stop()
        {
            //TODO added the drawing id
            _socket.SendMessage(new Tlv(SocketMessageTypes.EndDrawing));
            _timer.Stop();
            _editor.Canvas.MouseMove -= CanvasOnMouseMove;
            _editor.Canvas.MouseDown -= CanvasOnMouseDown;
            _editor.StrokeDeleted -= EditorOnStrokeDeleted;
            _editor.StrokedAdded -= EditorOnStokeAdded;
        }

        private void EditorOnStokeAdded(object sender, EventArgs args)
        {
            Stroke stroke = (Stroke) sender;
            stroke.AddPropertyData(GUIDs.ID, _currentStrokeID.ToString());
            Console.WriteLine(_currentStrokeID.ToString());
            SendStroke(true);
        }
        
        private void EditorOnStrokeDeleted(object sender, EventArgs args)
        {
            Stroke stroke = (Stroke) sender;
            _socket.SendMessage(new Tlv(SocketMessageTypes.DeleteStroke, new Guid(stroke.GetPropertyData(GUIDs.ID).ToString())));
            Console.WriteLine(new Guid(stroke.GetPropertyData(GUIDs.ID).ToString()));
            
        }

        private void TimerOnElapsed(object sender, ElapsedEventArgs e)
        {
            //Send all the points
            SendStroke(false);
        }

        private void SendStroke(bool startNewStroke)
        {
            if (_information.SelectedTool == InkCanvasEditingMode.EraseByStroke)
                return;
            _mutex.WaitOne();
            if(_currentStrokeID != Guid.Empty && _points.Count > 0)
            {
                byte[] message = new byte[POINTS_OFFSET + 4 * _points.Count];
                message[0] = (byte) (GetColorValue() + GetToolValue() + GetTipValue());
                //TODO validate bytes order
                _currentStrokeID.ToByteArray().CopyTo(message, 1);
                //message[MAX_X_OFFSET] = (byte) Math.Ceiling(_editor.Canvas.Width);
                //message[MAX_y_OFFSET] = (byte) Math.Ceiling(_editor.Canvas.Height);
                message[BRUSH_SIZE_OFFSET] = (byte) _information.BrushSize;
                for (int i = 0; i < _points.Count; i++)
                {
                    int x = (int) Math.Ceiling(_points[i].X);
                    int y = (int) Math.Ceiling(_points[i].Y);
                    message[POINTS_OFFSET + 4 * i] = GetIntByte(1, x);
                    message[POINTS_OFFSET + 4 * i + 1] = GetIntByte(2, x);
                    message[POINTS_OFFSET + 4 * i + 2] = GetIntByte(1, y);
                    message[POINTS_OFFSET + 4 * i + 3] = GetIntByte(2, y);
                }
                _socket.SendMessage(new Tlv(SocketMessageTypes.UserStrokeSent, message));
                if (startNewStroke)
                {
                    _currentStrokeID = Guid.Empty;
                }
                _points.Clear();
            }
            _mutex.ReleaseMutex();
        }

        private byte GetIntByte(int n, int value)
        {
            return (byte)((value >> (8*n)) & 0xff);
        }

        private void CanvasOnMouseDown(object sender, MouseButtonEventArgs e)
        {
            _mutex.WaitOne();
            _currentStrokeID = Guid.NewGuid();
            _mutex.ReleaseMutex();
        }

        private void CanvasOnMouseMove(object sender, MouseEventArgs e)
        {
            _mutex.WaitOne();
            if (_currentStrokeID != Guid.Empty)
            {
                _points.Add(e.GetPosition(_editor.Canvas));
            }
            _mutex.ReleaseMutex();
        }

        private int GetTipValue()
        {
            switch (_information.SelectedTip)
            {
                case StylusTip.Ellipse:
                    return 0;
                default:
                    return 1 << 6;
            }
        }

        private int GetToolValue()
        {
            switch (_information.SelectedTool)
            {
                case InkCanvasEditingMode.Ink:
                    return 0;
                case InkCanvasEditingMode.EraseByPoint:
                    return 1 << 7;
            }
            throw new InvalidOperationException();
        }
        private int GetColorValue()
        {
            if (_information.SelectedColor == Colors.Black)
                return 0;
            if (_information.SelectedColor == Colors.White)
                return 1;
            if (_information.SelectedColor == Colors.Red)
                return 2;
            if (_information.SelectedColor == Colors.Green)
                return 3;
            if (_information.SelectedColor == Colors.Blue)
                return 4;
            if (_information.SelectedColor == Colors.Yellow)
                return 5;
            if (_information.SelectedColor == Colors.Cyan)
                return 6;
            if (_information.SelectedColor == Colors.Magenta)
                return 7;
            throw new InvalidOperationException("the color is not selected");
        }
        
        
    }
}