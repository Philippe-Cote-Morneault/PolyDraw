using System;
using System.Globalization;
using System.Windows.Data;

namespace ClientLourd.Utilities.Converters
{
    public class NotificationConverter: IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            var notification = (int) value;
            if (notification < 1)
            {
                return "";
            }
            else if (notification > 99)
            {
                return "99+";
            }
            return notification.ToString();
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}