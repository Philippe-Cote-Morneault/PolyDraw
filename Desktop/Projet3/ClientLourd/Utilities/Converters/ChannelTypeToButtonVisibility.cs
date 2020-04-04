using System;
using System.Globalization;
using System.Windows;
using System.Windows.Data;
using ClientLourd.Models.Bindable;

namespace ClientLourd.Utilities.Converters
{
    public class ChannelTypeToButtonVisibility : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            Channel channel = (Channel) value;
            if (channel != null)
            {
                if (channel.IsGame || channel.IsGeneral)
                {
                    return Visibility.Hidden;
                }
            }

            return Visibility.Visible;
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}