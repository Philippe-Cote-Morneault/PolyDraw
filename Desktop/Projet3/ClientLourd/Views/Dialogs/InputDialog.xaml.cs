using System.Windows.Controls;

namespace ClientLourd.Views.Dialogs
{
    public partial class InputDialog : UserControl
    {
        public InputDialog(string message, int maxLength)
        {
            InitializeComponent();
            DataContext = this;
            MessageTextBlock.Text = message;
            FieldTextBox.Focus();
            FieldTextBox.MaxLength = maxLength;
        }

        public string Result { get; set; }
    }
}