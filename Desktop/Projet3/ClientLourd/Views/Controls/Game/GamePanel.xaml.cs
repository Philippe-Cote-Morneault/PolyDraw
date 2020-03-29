using ClientLourd.ViewModels;
using System.Windows.Controls;

namespace ClientLourd.Views.Controls.Game
{
    public partial class GamePanel : UserControl
    {
        public GamePanel()
        {
            InitializeComponent();
        }

        public void AfterLogin()
        {
            ViewModel.AfterLogin();
            TopBar.AfterLogin();
            EditorZone.AfterLogin();
            GameStatus.AfterLogin();
        }

        public void AfterLogout()
        {
            ViewModel.AfterLogOut();
        }
    }
}