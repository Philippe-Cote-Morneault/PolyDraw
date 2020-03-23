using ClientLourd.Utilities.Commands;
using ClientLourd.Views.Dialogs;
using MaterialDesignThemes.Wpf;
using System.Windows.Input;

namespace ClientLourd.Services.ProfileViewerService
{
    public class ProfileViewer
    {
        static private RelayCommand<object> _viewPublicProfileCommand;

        static public ICommand ViewPublicProfileCommand
        {
            get
            {
                return _viewPublicProfileCommand ??
                       (_viewPublicProfileCommand = new RelayCommand<object>(param => OpenPublicProfile()));
            }
        }

        static private void OpenPublicProfile() 
        {
            DialogHost.Show(new ClosableErrorDialog("Hi!"), "Default");

        }


    }
}
