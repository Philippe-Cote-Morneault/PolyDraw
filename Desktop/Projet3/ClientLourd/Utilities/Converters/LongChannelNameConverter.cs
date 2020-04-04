using System;
using System.Globalization;
using System.Windows;
using System.Windows.Data;
using ClientLourd.Models.Bindable;
using ClientLourd.Utilities.Enums;
using ClientLourd.ViewModels;

namespace ClientLourd.Utilities.Converters
{
    public class LongChannelNameConverter : IMultiValueConverter
    {
        public object Convert(object[] values, Type targetType, object parameter, CultureInfo culture)
        {
            Channel channel = (Channel) values[0];
            Languages language = (Languages) values[1];
            if (channel == null)
                return "";
            if (channel.IsGame)
            {
                return language == Languages.EN ? "Active Match" : "Partie en cours";
            }

            if (channel.ID == Guid.Empty.ToString())
            {
                return language == Languages.EN ? "General" : "Général";
            }

            return channel.Name;
        }

        public object[] ConvertBack(object value, Type[] targetTypes, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}