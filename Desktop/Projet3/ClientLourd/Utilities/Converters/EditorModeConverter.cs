using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Data;
using System.Windows.Controls;


namespace ClientLourd.Utilities.Converters
{
    /// <summary>
    /// Permet au InkCanvas de définir son mode d'édition en fonction de l'outil sélectionné.
    /// </summary>
    public class EditorModeConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture)
        {
            switch (value)
            {
                case "efface_trait":
                    return InkCanvasEditingMode.EraseByStroke;
                default:
                    return InkCanvasEditingMode.Ink;
            }
        }

        public object ConvertBack(object value, Type targetType, object parameter,
            System.Globalization.CultureInfo culture) => System.Windows.DependencyProperty.UnsetValue;
    }
}