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
   
    public sealed class ComparatorToVisibilityConverter : IMultiValueConverter
    {
    
        public object Convert(object[] values, Type targetType, object parameter, CultureInfo culture)
        {
            if (parameter != null && parameter.ToString() == "1")
            {
                return (values.All(v => v.ToString() == values[0].ToString())) ? Visibility.Visible : Visibility.Collapsed;   
            }
            return (values.All(v => v.ToString() == values[0].ToString())) ? Visibility.Collapsed : Visibility.Visible;   
        }


        public object[] ConvertBack(
            object value, Type[] targetTypes, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}
