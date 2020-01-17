using System;
using System.ComponentModel;
using System.Windows.Data;

namespace ClientLourd.Utilities.Converters
{
    public class DivideValueConverter: IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture)
        {
            if (value != null)
                if (parameter != null)
                    return ((double) value) / int.Parse((string)parameter);
            throw new InvalidEnumArgumentException();
        }

        public object ConvertBack(object value, Type targetType, object parameter,
            System.Globalization.CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}