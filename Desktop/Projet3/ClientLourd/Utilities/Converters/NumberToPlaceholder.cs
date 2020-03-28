using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Data;
using ClientLourd.Models.Bindable;

namespace ClientLourd.Utilities.Converters
{
    class NumberToPlaceholder : IValueConverter
    {

        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            if (value == null)
            {
                return new User[1];
            }

            Lobby currentLobby = (Lobby)value;
            int nPlaceHolder = currentLobby.PlayersMax - currentLobby.Players.Count;

            if (nPlaceHolder < 0)
                return null;

            User[] users = new User[nPlaceHolder];
            for(int i = 0; i < nPlaceHolder; i++)
            {
                users[i] = new User("Empty slot", "", false);
            }

            return users;
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}
