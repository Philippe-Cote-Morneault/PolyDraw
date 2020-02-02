using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Shapes;
using ClientLourd.ModelViews;
using ClientLourd.Views;

namespace ClientLourd.Utilities.Window
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