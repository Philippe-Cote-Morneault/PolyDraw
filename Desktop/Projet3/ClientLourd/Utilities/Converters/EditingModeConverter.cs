using System;
using System.Globalization;
using System.Windows.Controls;
using System.Windows.Data;

namespace ClientLourd.Utilities.Converters
{
    public class EditingModeConverter: IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            var mode = (InkCanvasEditingMode) value;
            if (mode == InkCanvasEditingMode.EraseByPoint)
            {
                return InkCanvasEditingMode.Ink;
            }

            return mode;
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}