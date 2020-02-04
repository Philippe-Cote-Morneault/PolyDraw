using System.Windows.Controls;
using ClientLourd.ViewModels;

namespace ClientLourd.Views.Controls
{
    public partial class Chat : UserControl
    {
        public Chat()
        {
            InitializeComponent();
            DataContext = new ChatViewModel();
        }

        public void Init()
        {
            ((ChatViewModel) DataContext).Init();
        }
    }
}