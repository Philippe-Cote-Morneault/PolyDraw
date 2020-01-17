using System;
using System.ComponentModel;
using System.Windows.Data;

namespace ClientLourd.Utilities.Converters
{
    public class ComparatorToBooleanConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture)
        {
            if (value != null)
                if (parameter != null)
                    return value.ToString() == parameter.ToString();
            throw new InvalidEnumArgumentException();
        }

        public object ConvertBack(object value, Type targetType, object parameter,
            System.Globalization.CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}