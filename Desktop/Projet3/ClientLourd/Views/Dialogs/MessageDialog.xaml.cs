using System.Windows.Controls;

namespace ClientLourd.Views.Dialogs
{
    public partial class MessageDialog : UserControl
    {
        public MessageDialog(string title, string message)
        {
            InitializeComponent();
            TitleTextBlock.Text = title;
            MessageTextBlock.Text = message;
        }
    }
}