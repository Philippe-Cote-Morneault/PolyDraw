using ClientLourd.Utilities.Commands;
using ClientLourd.Views.Dialogs;
using MaterialDesignThemes.Wpf;
using System.Windows.Input;
using ClientLourd.Models.Bindable;
using System.Windows;

namespace ClientLourd.Services.ProfileViewerService
{
    public class ProfileViewer
    {
        static private RelayCommand<User> _viewPublicProfileCommand;

        static public ICommand ViewPublicProfileCommand
        {
            get
            {
                return _viewPublicProfileCommand ??
                       (_viewPublicProfileCommand = new RelayCommand<User>(param => OpenPublicProfile(param)));
            }
        }

        static private async void OpenPublicProfile(User param) 
        {
            PublicProfileDialog publicProfileDialog = new PublicProfileDialog(param);

            await DialogHost.Show(publicProfileDialog, "Default", (object o, DialogClosingEventArgs closingEventHandler) =>
            {
                (((MainWindow)Application.Current.MainWindow).MainWindowDialogHost as DialogHost).CloseOnClickAway = false;
            });
        }


    }
}
