using System.ComponentModel;
using ClientLourd.ViewModels;
using ClientLourd.Views.Controls;

namespace ClientLourd.Views.Windows
{
    /// <summary>
    /// Interaction logic for SimpleWindow.xaml
    /// </summary>
    public partial class ChatWindow : System.Windows.Window
    {
        Chat _chatBox;

        public ChatWindow(Chat ChatBox)
        {
            _chatBox = ChatBox;
            InitializeComponent();
            MainPanel.Children.Add(ChatBox);
            ((ChatViewModel)ChatBox.DataContext).OnChatToggle(true);
            Closing += OnWindowClosing;
        }

        public void OnWindowClosing(object sender, CancelEventArgs e)
        {
            MainPanel.Children.Clear();
            ((MainWindow) Owner).ReturnTheChat();
        }
    }
}