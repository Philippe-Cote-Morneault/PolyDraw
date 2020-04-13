using System.Windows.Controls;
using System.Windows.Ink;
using System.Windows.Media;

namespace ClientLourd.Models.Bindable
{
    public class EditorInformation : ModelBase
    {
        public EditorInformation()
        {
            BrushSize = 12;
            SelectedTool = InkCanvasEditingMode.Ink;
            SelectedTip = StylusTip.Ellipse;
            SelectedColor = Colors.Black;
            IsAnEraser = false;
        }


        private StrokeCollection _strokes = new StrokeCollection();

        private DrawingAttributes _drawingAttributes = new DrawingAttributes();

        public DrawingAttributes DrawingAttribtes
        {
            get { return _drawingAttributes; }
            set
            {
                _drawingAttributes = value;
                NotifyPropertyChanged();
            }
        }


        public StrokeCollection Strokes
        {
            get { return _strokes; }
            set
            {
                _strokes = value;
                NotifyPropertyChanged();
            }
        }

        private InkCanvasEditingMode _selectedTool;

        public InkCanvasEditingMode SelectedTool
        {
            get { return _selectedTool; }
            set
            {
                if (value == InkCanvasEditingMode.EraseByPoint)
                {
                    IsAnEraser = true;
                    SelectedColor = Colors.White;
                }
                else
                {
                    IsAnEraser = false;
                }

                _selectedTool = value;
                NotifyPropertyChanged();
            }
        }

        private StylusTip _selectedTip;

        public StylusTip SelectedTip
        {
            get { return _selectedTip; }
            set
            {
                _selectedTip = value;
                DrawingAttribtes.StylusTip = value;
                NotifyPropertyChanged();
            }
        }

        // Couleur des traits tracés par le crayon.
        private Color _selectedColor;

        public Color SelectedColor
        {
            get { return _selectedColor; }
            set
            {
                _selectedColor = value;
                DrawingAttribtes.Color = value;
                NotifyPropertyChanged();
            }
        }

        // Grosseur des traits tracés par le crayon.
        private int _brushSize;

        public int BrushSize
        {
            get { return _brushSize; }
            set
            {
                _brushSize = value;
                DrawingAttribtes.Width = BrushSize;
                DrawingAttribtes.Height = BrushSize;
                NotifyPropertyChanged();
            }
        }

        private bool _isAnEraser;

        public bool IsAnEraser
        {
            get { return _isAnEraser; }
            set
            {
                if (_isAnEraser != value)
                {
                    _isAnEraser = value;
                    NotifyPropertyChanged();
                }
            }
        }
    }
}