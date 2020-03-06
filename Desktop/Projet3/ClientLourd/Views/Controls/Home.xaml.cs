using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using ClientLourd.Models.Bindable;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using ClientLourd.Views.Dialogs;
using MaterialDesignThemes.Wpf;
using ClientLourd.ViewModels;

namespace ClientLourd.Views.Controls
{
    /// <summary>
    /// Interaction logic for Home.xaml
    /// </summary>
    public partial class Home : UserControl
    {
        public Home()
        {
            InitializeComponent();
        }

        public HomeViewModel HomeViewModel
        {
            get => (DataContext as HomeViewModel);
        }

        private void CreateLobby(object sender, RoutedEventArgs e)
        {
            DialogHost.Show(new LobbyCreationDialog(), "Default");
        }

        private void CreateGame(object sender, RoutedEventArgs e)
        {
            DialogHost.Show(new GameCreationDialog(), "Default");
        }

        private void OnLobbySelection(object sender, EventArgs e)
        {

            Models.Bindable.Lobby lobbySelected = (((ContentControl)sender).Content) as Models.Bindable.Lobby;
            if (lobbySelected != null)
            {
                HomeViewModel.JoinLobby(lobbySelected);
            }
        }

    }
}
