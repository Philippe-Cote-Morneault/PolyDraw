using System;
using System.Collections;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Globalization;
using System.Windows.Data;
using System.Windows.Documents;
using ClientLourd.Models.Bindable;

namespace ClientLourd.Utilities.Converters
{
    public class PlayerToPositionConverter: IMultiValueConverter
    {
        public object Convert(object[] values, Type targetType, object parameter, CultureInfo culture)
        {
            var item = (Player)values[0];
            var items = (ObservableCollection<Player>) values[1];
            return (items.IndexOf(item) + 1).ToString();
        }

        public object[] ConvertBack(object value, Type[] targetTypes, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}