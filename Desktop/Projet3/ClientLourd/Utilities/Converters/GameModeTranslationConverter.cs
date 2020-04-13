using System;
using System.Globalization;
using System.Windows.Data;
using ClientLourd.Utilities.Enums;

namespace ClientLourd.Utilities.Converters
{
    public class GameModeTranslationConverter: IMultiValueConverter
    {
        public object Convert(object[] values, Type targetType, object parameter, CultureInfo culture)
        {
            string mode = (string)values[0];
            Languages language = (Languages) values[1];
            if (mode == "FFA")
            {
                return language == Languages.EN ? mode : "Mêlée générale";
            }
            if (mode == "Coop")
            {
                return language == Languages.EN ? mode : "Coopératif";
            }

            return mode;
        }

        public object[] ConvertBack(object value, Type[] targetTypes, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}