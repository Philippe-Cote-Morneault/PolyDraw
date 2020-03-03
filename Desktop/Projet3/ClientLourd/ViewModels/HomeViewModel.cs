using ClientLourd.Models.Bindable;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ClientLourd.Utilities.Enums;

namespace ClientLourd.ViewModels
{
    class HomeViewModel : ViewModelBase
    {
        private ObservableCollection<Lobby> _lobbies;

        public HomeViewModel()
        {
            Lobbies = new ObservableCollection<Lobby>();
            Lobbies.Add(new Lobby("My nice lobby come join COOP", "TamereShortz", GameModes.Coop, 8));
            Lobbies.Add(new Lobby("My nice lobby come join SOLO", "Tame2", GameModes.Solo, 1));
            Lobbies.Add(new Lobby("FFA", "FFALover", GameModes.FFA, 8));
        }

        public override void AfterLogin()
        {
            
            // GET
        }

        public override void AfterLogOut()
        {
            //??
        }

        public ObservableCollection<Lobby> Lobbies
        {
            get => _lobbies;
            set
            {
                if (value != _lobbies)
                {
                    _lobbies = value;
                    NotifyPropertyChanged();
                }
            }
        }
    }
}
