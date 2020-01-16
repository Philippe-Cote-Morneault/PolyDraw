using System.Windows.Controls;
using ClientLourd.ModelViews;

namespace ClientLourd.Views
{
    public partial class Chat : UserControl
    {
        public Chat()
        {
            InitializeComponent();
            DataContext = new ChatViewModel();
        }
    }
}