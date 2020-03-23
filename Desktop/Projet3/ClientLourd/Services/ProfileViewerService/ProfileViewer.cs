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
        static private RelayCommand<Message> _viewPublicProfileCommand;

        static public ICommand ViewPublicProfileCommand
        {
            get
            {
                return _viewPublicProfileCommand ??
                       (_viewPublicProfileCommand = new RelayCommand<Message>(param => OpenPublicProfile(param)));
            }
        }

        static private async void OpenPublicProfile(Message param) 
        {
            //DialogHost.Show(new PublicProfileDialog(param.User), "Default");
            PublicProfileDialog publicProfileDialog = new PublicProfileDialog(param.User);

            await DialogHost.Show(publicProfileDialog, (object o, DialogClosingEventArgs closingEventHandler) =>
            {
                (((MainWindow)Application.Current.MainWindow).MainWindowDialogHost as DialogHost).CloseOnClickAway = false;
            });
        }


    }
}
