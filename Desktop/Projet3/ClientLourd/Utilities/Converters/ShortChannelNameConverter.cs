using System;
using System.Globalization;
using System.Windows.Data;
using ClientLourd.Models.Bindable;
using ClientLourd.Utilities.Enums;

namespace ClientLourd.Utilities.Converters
{
    public class ShortChannelNameConverter: IMultiValueConverter
    {
        
        public object Convert(object[] values, Type targetType, object parameter, CultureInfo culture)
        {
            string name = "";
            Channel channel = (Channel) values[0];
            Languages language = (Languages) values[1];
            if (channel == null)
                return name;
            if (channel.IsGame)
            {
                name = language == Languages.EN ? "Active Match" : "Partie en cours";
            }
            else if (channel.ID == Guid.Empty.ToString())
            {
                name =  language == Languages.EN ? "General" : "Général";
            }
            else
            {
                name = channel.Name;
            }
            int length = int.Parse(parameter.ToString());
            if (name.Length <= length)
            {
                return name;
            }
            return $"{name.Substring(0, length)}...";
            
        }

        public object[] ConvertBack(object value, Type[] targetTypes, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}