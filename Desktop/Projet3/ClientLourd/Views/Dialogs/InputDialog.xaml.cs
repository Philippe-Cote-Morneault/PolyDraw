using System.Windows.Controls;

namespace ClientLourd.Views.Dialogs
{
    public partial class InputDialog : UserControl
    {
        public InputDialog(string message)
        {
            InitializeComponent();
            DataContext = this;
            MessageTextBlock.Text = message;
        }
        
        public string Result { get; set; }
    }
}