using System;
using System.ComponentModel;
using System.Runtime.CompilerServices;
using System.Windows.Controls;
using System.Windows.Data;

namespace ClientLourd.Views.Dialogs
{
    public partial class ErrorDialog : UserControl, INotifyPropertyChanged
    {
        public ErrorDialog(Exception e)
        {
            InitializeComponent();
            DataContext = this;
            Error = e.Message;
        }

        public ErrorDialog(string message)
        {
            InitializeComponent();
            Error = message;
        }

        public string Error 
        {
            get { return _error; }
            set
            {
                _error = value;
                OnPropertyChanged();
            }
        }

        private string _error;

        public event PropertyChangedEventHandler PropertyChanged;

        protected virtual void OnPropertyChanged([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
    }
}