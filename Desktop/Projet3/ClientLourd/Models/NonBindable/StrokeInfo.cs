using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Ink;
using System.Windows.Input;
using System.Windows.Media;
using ClientLourd.Utilities.Constants;

namespace ClientLourd.Models.NonBindable
{
    public class StrokeInfo
    {
        private Guid _strokeID;
        private Guid _userID;
        private StylusPointCollection _pointCollection;
        private Color _strokeColor;
        private int _brushSize;
        private StylusTip _brushTip;
        private bool _isAnEraser;

        // Number of points in _pointCollection.Count
        // MaxX and MaxY useless since the canvas size is fix for clients

        public StrokeInfo(byte[] data)
        {
            _pointCollection = new StylusPointCollection();
            ParseData(data);
        }

        private void ParseData(byte[] data)
        {
            SetIsAnEraser(data[0]);
            SetColor(data[0]);
            SetBrushTip(data[0]);
            SetBrushSize(data.Skip(StrokeMessageOffsets.BRUSH_SIZE).Take(1).ToArray()[0]);
            SetStrokeUID(data.Skip(StrokeMessageOffsets.STROKE_ID).Take(16).ToArray());
            SetUserID(data.Skip(StrokeMessageOffsets.USER_ID).Take(16).ToArray());
            SetPoints(data.Skip(StrokeMessageOffsets.POINTS).ToArray());
        }

        private void SetBrushTip(byte firstByte)
        {
            BrushTip = ((firstByte & 0x40) == 0) ? StylusTip.Ellipse : StylusTip.Rectangle;
        }

        private void SetBrushSize(byte brushSizeByte)
        {
            BrushSize = brushSizeByte;
        }

        private void SetIsAnEraser(byte firstByte)
        {
            IsAnEraser = (firstByte >> 7) == 1;
        }

        private void SetColor(byte firstByte)
        {
            StrokeColor = NumberToColor(firstByte & 0x0F);
        }

        private Color NumberToColor(int colorNumber)
        {
            switch (colorNumber)
            {
                case 0:
                    return Colors.Black;
                case 1:
                    return Colors.White;
                case 2:
                    return Colors.Red;
                case 3:
                    return Colors.Green;
                case 4:
                    return Colors.Blue;
                case 5:
                    return Colors.Yellow;
                case 6:
                    return Colors.Cyan;
                case 7:
                    return Colors.Magenta;
                default:
                    return Colors.Black;
            }
        }


        private void SetStrokeUID(byte[] strokeUID)
        {
            if (BitConverter.IsLittleEndian)
                Array.Reverse(strokeUID);

            StrokeID = new Guid(strokeUID);
        }

        private void SetUserID(byte[] userUID)
        {
            if (BitConverter.IsLittleEndian)
                Array.Reverse(userUID);

            UserID = new Guid(userUID);
        }

        private void SetPoints(byte[] points)
        {
            if (BitConverter.IsLittleEndian)
                Array.Reverse(points);

            for (int i = 0; i < points.Length; i += 4)
            {
                int xPoint = BitConverter.ToUInt16(points.Skip(i).Take(2).ToArray(), 0);
                int yPoint = BitConverter.ToUInt16(points.Skip(i + 2).Take(2).ToArray(), 0);
                PointCollection.Add(new StylusPoint(xPoint, yPoint));
            }
        }

        public int BrushSize
        {
            get => _brushSize;
            set => _brushSize = value;
        }

        public StylusTip BrushTip
        {
            get => _brushTip;
            set => _brushTip = value;
        }

        public Guid StrokeID
        {
            get => _strokeID;
            set => _strokeID = value;
        }

        public Guid UserID
        {
            get => _userID;
            set => _userID = value;
        }

        public StylusPointCollection PointCollection
        {
            get => _pointCollection;
            set => _pointCollection = value;
        }

        public Color StrokeColor
        {
            get => _strokeColor;
            set => _strokeColor = value;
        }

        public bool IsAnEraser
        {
            get => _isAnEraser;
            set => _isAnEraser = value;
        }
    }
}


