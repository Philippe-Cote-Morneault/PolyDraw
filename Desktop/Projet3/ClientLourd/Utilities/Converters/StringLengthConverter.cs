using System;
using System.Globalization;
using System.Windows.Data;

namespace ClientLourd.Utilities.Converters
{
    public class StringLengthConverter: IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            string text = (string) value;
            if (text != null)
            {
                int length = int.Parse(parameter.ToString());
                if (text.Length <= length)
                {
                    return text;
                }
                return $"{text.Substring(0, length)}...";
            }

            return "";

        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}