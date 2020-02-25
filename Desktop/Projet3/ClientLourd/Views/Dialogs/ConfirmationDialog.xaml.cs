using System.Windows.Controls;

namespace ClientLourd.Views.Dialogs
{
    public partial class ConfirmationDialog : UserControl
    {
        public ConfirmationDialog(string title, string message)
        {
            InitializeComponent();
            TitleTextBlock.Text = title;
            MessageTextBlock.Text = message;
        }
    }
}