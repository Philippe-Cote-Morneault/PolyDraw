using ClientLourd.Utilities.Commands;
using ClientLourd.Views.Dialogs;
using MaterialDesignThemes.Wpf;
using System.Windows.Input;
using ClientLourd.Models.Bindable;

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

        static private void OpenPublicProfile(Message param) 
        {
            DialogHost.Show(new PublicProfileDialog(), "Default");

        }


    }
}
