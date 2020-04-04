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
    class TimeDifferenceConverter : IMultiValueConverter
    {
        /// <summary>
        /// Returns the time elapsed between two dates. Value is the disconnection time and parameter is the connected time.
        /// </summary>
        /// <param name="value"></param>
        /// <param name="targetType"></param>
        /// <param name="parameter"></param>
        /// <param name="culture"></param>
        /// <returns></returns>
        public object Convert(object[] value, Type targetType, object parameter, CultureInfo culture)
        {
            DateTime dtDisconnection = Timestamp.UnixTimeStampToDateTime((long) value[0]);
            DateTime dtConnection = Timestamp.UnixTimeStampToDateTime((long) value[1]);


            return (dtConnection - dtDisconnection).ToString();
        }


        public object[] ConvertBack(object value, Type[] targetTypes, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}