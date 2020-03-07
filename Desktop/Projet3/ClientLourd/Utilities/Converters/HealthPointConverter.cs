using System;
using System.Globalization;
using System.Windows;
using System.Windows.Data;

namespace ClientLourd.Utilities.Converters
{
    public class HealthPointConverter: IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            int hp = (int) value;
            if (parameter != null && hp >= int.Parse(parameter.ToString()))
            {
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