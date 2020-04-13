using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Data;
using ClientLourd.Utilities.Enums;

namespace ClientLourd.Utilities.Converters
{
    class ModeAndIsCPUToVisibility : IMultiValueConverter
    {
        public object Convert(object[] values, Type targetType, object parameter, CultureInfo culture)
        {
            GameModes mode = (GameModes) values[0];
            bool isCPU = (bool) values[1];

            if (mode == GameModes.FFA || !isCPU)
            {
                return Visibility.Visible;
            }

            return Visibility.Collapsed;
        }

        public object[] ConvertBack(object value, Type[] targetTypes, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}