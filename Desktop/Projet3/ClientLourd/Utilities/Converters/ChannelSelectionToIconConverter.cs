using System;
using System.Globalization;
using System.Windows.Data;
using ClientLourd.Models.Bindable;
using MaterialDesignThemes.Wpf;

namespace ClientLourd.Utilities.Converters
{
    public class ChannelSelectionToIconConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            var channel = (Channel) value;
            if (channel != null)
            {
                if (channel.ID == Guid.Empty.ToString())
                {
                    return PackIconKind.UsersGroup;
                }

                if (channel.IsGame)
                {
                    return PackIconKind.GamepadVariant;
                }
            }

            return PackIconKind.Comments;
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}