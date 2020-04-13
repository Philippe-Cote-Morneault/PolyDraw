using System;
using System.Globalization;
using System.Windows;
using System.Windows.Data;
using ClientLourd.Models.Bindable;

namespace ClientLourd.Utilities.Converters
{
    public class ChannelSelectionBorderConverter : IMultiValueConverter
    {
        public object Convert(object[] values, Type targetType, object parameter, CultureInfo culture)
        {
            Channel selectedChannel = (Channel) values[0];
            Channel currentChannel = (Channel) values[1];
            if (selectedChannel != null && currentChannel != null)
            {
                return selectedChannel.ID == currentChannel.ID ? new Thickness(2) : new Thickness(0);
            }

            return new Thickness(0);
        }

        public object[] ConvertBack(object value, Type[] targetTypes, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}