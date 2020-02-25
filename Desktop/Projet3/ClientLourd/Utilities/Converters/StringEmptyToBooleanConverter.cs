using System;
using System.Globalization;
using System.Windows.Data;

namespace ClientLourd.Utilities.Converters
{
    public class StringEmptyToBooleanConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            var text = (string) value;
            return !String.IsNullOrWhiteSpace(text);
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}