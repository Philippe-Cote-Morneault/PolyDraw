using System;
using System.Globalization;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;

namespace ClientLourd.Utilities.Converters
{
    public class EditingModeToVisibility: IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            if (value != null)
            {
                var mode = (InkCanvasEditingMode)value;
                if (mode == InkCanvasEditingMode.Ink || mode == InkCanvasEditingMode.EraseByPoint)
                    return Visibility.Visible;
            }
            return Visibility.Collapsed;
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}