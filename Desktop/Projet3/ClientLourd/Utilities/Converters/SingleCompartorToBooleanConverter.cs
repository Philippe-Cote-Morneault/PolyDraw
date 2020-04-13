using System;
using System.Globalization;
using System.Windows.Data;

namespace ClientLourd.Utilities.Converters
{
    public class SingleCompartorToBooleanConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            return (value != null && (parameter != null && (value.ToString() == parameter.ToString())));
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}