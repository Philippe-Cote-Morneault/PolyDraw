using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Data;
using System.Windows.Media.Imaging;
using ClientLourd.Utilities.Enums;

namespace ClientLourd.Utilities.Converters
{
    class GameModeToImageConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            var gameMode = (GameModes)value;

            if (gameMode == GameModes.Coop)
                return new BitmapImage(new Uri($"/ClientLourd;component/Resources/treasure.png", UriKind.Relative));
            if (gameMode == GameModes.FFA)
                return new BitmapImage(new Uri($"/ClientLourd;component/Resources/swords.png", UriKind.Relative));
            if (gameMode == GameModes.Solo)
                return new BitmapImage(new Uri($"/ClientLourd;component/Resources/sword.png", UriKind.Relative));
            
            throw new Exception("Not image corresponding to game mode");
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}
