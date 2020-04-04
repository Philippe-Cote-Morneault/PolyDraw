using ClientLourd.Utilities.Enums;
using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Data;

namespace ClientLourd.Utilities.Converters
{
    class ModeToVisibilityConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            string parameterString = parameter as string;
            if (!string.IsNullOrEmpty(parameterString))
            {
                string[] parameters = parameterString.Split(new char[] {'|'});
                foreach (string param in parameters)
                {
                    if (Enum.TryParse(param, out GameModes res))
                    {
                        if (res == (GameModes) value)
                        {
                            return Visibility.Visible;
                        }
                    }
                }
            }

            return Visibility.Collapsed;
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}