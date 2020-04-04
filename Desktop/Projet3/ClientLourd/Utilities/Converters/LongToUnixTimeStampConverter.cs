using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Data;
using ClientLourd.Services.DateService;

namespace ClientLourd.Utilities.Converters
{
    class LongToUnixTimeStampConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            double time = System.Convert.ToDouble(value);
            return Timestamp.UnixTimeStampToDateTime(time).ToString();
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            return value != null && !(bool) value;
        }
    }
}