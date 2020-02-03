using System.ComponentModel;

namespace ClientLourd.Views.Window
{
    /// <summary>
    /// Interaction logic for SimpleWindow.xaml
    /// </summary>
    public partial class ChatWindow : System.Windows.Window
    {
        Chat _chatBox;

        public ChatWindow(ClientLourd.Views.Chat ChatBox)
        {
            _chatBox = ChatBox;
            InitializeComponent();
            MainStackPanel.Children.Add(ChatBox);
            Closing += OnWindowClosing;
        }

        public void OnWindowClosing(object sender, CancelEventArgs e)
        {
            MainStackPanel.Children.Clear();
            ((MainWindow) Owner).RightDrawerContent.Children.Add(_chatBox);
            ((MainWindow) Owner).ChatToggleButton.IsEnabled = true;
            ((MainWindow) Owner).ClearChatNotification();
        }
    }
}