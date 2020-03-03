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
    class WidthToLeftMarginConverter : IMultiValueConverter
    {
        

        public object Convert(object[] values, Type targetType, object parameter, CultureInfo culture)
        {
            var cellWidth = (double)values[1];
            var imageWidth = (double)values[0];
            var leftMargin = (cellWidth / 2) - imageWidth;

            return new Thickness(leftMargin, 0, 0, 0);
        }


        public object[] ConvertBack(object value, Type[] targetTypes, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}
