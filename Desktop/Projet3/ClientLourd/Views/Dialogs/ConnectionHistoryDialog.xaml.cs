using ClientLourd.Utilities.Commands;
using MaterialDesignThemes.Wpf;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.ComponentModel;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using System.Timers;

namespace ClientLourd.Views.Dialogs
{
    /// <summary>
    /// Interaction logic for ConnectionHistoryDialog.xaml
    /// </summary>
    public partial class ConnectionHistoryDialog : UserControl, INotifyPropertyChanged
    {
        private LinkedList<Employee> _myList;
        private Timer _scrollToBottomTimer;

        public ConnectionHistoryDialog()
        {

            (((MainWindow)Application.Current.MainWindow).MainWindowDialogHost as DialogHost).CloseOnClickAway = true;

            MyList = new LinkedList<Employee>();
            MyList.AddLast(new Employee() { Name = "John Doe", Age = 42, Mail = "john@doe-family.com" });
            MyList.AddLast(new Employee() { Name = "Jane Doe", Age = 39, Mail = "jane@doe-family.com" });
            MyList.AddLast(new Employee() { Name = "Sammy Doe", Age = 7, Mail = "sammy.doe@gmail.com" });
            MyList.AddLast(new Employee() { Name = "Sammy Doe", Age = 7, Mail = "sammy.doe@gmail.com" });

            MyList.AddLast(new Employee() { Name = "Sammy Doe", Age = 7, Mail = "sammy.doe@gmail.com" });
            MyList.AddLast(new Employee() { Name = "John Doe", Age = 42, Mail = "john@doe-family.com" });
            MyList.AddLast(new Employee() { Name = "Jane Doe", Age = 39, Mail = "jane@doe-family.com" });
            MyList.AddLast(new Employee() { Name = "Sammy Doe", Age = 7, Mail = "sammy.doe@gmail.com" });
            MyList.AddLast(new Employee() { Name = "Sammy Doe", Age = 7, Mail = "sammy.doe@gmail.com" });

            MyList.AddLast(new Employee() { Name = "Sammy Doe", Age = 7, Mail = "sammy.doe@gmail.com" });
            MyList.AddLast(new Employee() { Name = "John Doe", Age = 42, Mail = "john@doe-family.com" });
            MyList.AddLast(new Employee() { Name = "Jane Doe", Age = 39, Mail = "jane@doe-family.com" });
            MyList.AddLast(new Employee() { Name = "Sammy Doe", Age = 7, Mail = "sammy.doe@gmail.com" });
            MyList.AddLast(new Employee() { Name = "Sammy Doe", Age = 7, Mail = "sammy.doe@gmail.com" });

            MyList.AddLast(new Employee() { Name = "Sammy Doe", Age = 7, Mail = "sammy.doe@gmail.com" });
            MyList.AddLast(new Employee() { Name = "John Doe", Age = 42, Mail = "john@doe-family.com" });
            MyList.AddLast(new Employee() { Name = "Jane Doe", Age = 39, Mail = "jane@doe-family.com" });
            MyList.AddLast(new Employee() { Name = "Sammy Doe", Age = 7, Mail = "sammy.doe@gmail.com" });
            MyList.AddLast(new Employee() { Name = "Sammy Doe", Age = 7, Mail = "sammy.doe@gmail.com" });
            MyList.AddLast(new Employee() { Name = "John Doe", Age = 42, Mail = "john@doe-family.com" });
            MyList.AddLast(new Employee() { Name = "Jane Doe", Age = 39, Mail = "jane@doe-family.com" });
            MyList.AddLast(new Employee() { Name = "Sammy Doe", Age = 7, Mail = "sammy.doe@gmail.com" });
            MyList.AddLast(new Employee() { Name = "Sammy Doe", Age = 7, Mail = "sammy.doe@gmail.com" });

            MyList.AddLast(new Employee() { Name = "Sammy Doe", Age = 7, Mail = "sammy.doe@gmail.com" });
            MyList.AddLast(new Employee() { Name = "John Doe", Age = 42, Mail = "john@doe-family.com" });
            MyList.AddLast(new Employee() { Name = "Jane Doe", Age = 39, Mail = "jane@doe-family.com" });
            MyList.AddLast(new Employee() { Name = "Sammy Doe", Age = 7, Mail = "sammy.doe@gmail.com" });
            MyList.AddLast(new Employee() { Name = "Sammy Doe", Age = 7, Mail = "sammy.doe@gmail.com" });

            MyList.AddLast(new Employee() { Name = "Sammy Doe", Age = 7, Mail = "sammy.doe@gmail.com" });
            MyList.AddLast(new Employee() { Name = "John Doe", Age = 42, Mail = "john@doe-family.com" });
            MyList.AddLast(new Employee() { Name = "Jane Doe", Age = 39, Mail = "jane@doe-family.com" });
            MyList.AddLast(new Employee() { Name = "Sammy Doe", Age = 7, Mail = "sammy.doe@gmail.com" });
            MyList.AddLast(new Employee() { Name = "Sammy Doe", Age = 7, Mail = "sammy.doe@gmail.com" });

            MyList.AddLast(new Employee() { Name = "Sammy Doe", Age = 7, Mail = "sammy.doe@gmail.com" });
            MyList.AddLast(new Employee() { Name = "John Doe", Age = 42, Mail = "john@doe-family.com" });
            MyList.AddLast(new Employee() { Name = "Jane Doe", Age = 39, Mail = "jane@doe-family.com" });
            MyList.AddLast(new Employee() { Name = "Sammy Doe", Age = 7, Mail = "sammy.doe@gmail.com" });
            MyList.AddLast(new Employee() { Name = "Sammy Doe", Age = 7, Mail = "sammy.doe@gmail.com" });

            MyList.AddLast(new Employee() { Name = "Sammy Doe", Age = 7, Mail = "sammy.doe@gmail.com" });
            MyList = new LinkedList<Employee>(MyList);

            InitializeComponent();
            _scrollToBottomTimer = new Timer(400);
            _scrollToBottomTimer.Elapsed += ScrollToBottom;
            _scrollToBottomTimer.Start();
        }

        public void ScrollToBottom(object sender, ElapsedEventArgs e)
        {
            _scrollToBottomTimer.Stop();
            Application.Current.Dispatcher.InvokeAsync(() =>
            {
                ScrollViewerElement.ScrollToBottom();
            });
            
        }


        public LinkedList<Employee> MyList
        {
            get { return _myList; }
            set
            {
                if (value != _myList)
                {
                    _myList = value;
                    NotifyPropertyChanged();
                }
            }
        }

        private void ScrollViewer_OnScrollChanged(object sender, ScrollChangedEventArgs e)
        {
            ScrollViewer scroll = sender as ScrollViewer;
            if (scroll == null)
            {
                throw new InvalidOperationException(
                    "The attached AlwaysScrollToEnd property can only be applied to ScrollViewer instances.");
            }
            if (e.ExtentHeightChange == 0 && scroll.VerticalOffset == 0)
            {
                LinkedList<Employee> linkl = new LinkedList<Employee>();
                linkl.AddLast(new Employee() { Name = "New", Age = 7, Mail = "New" });
                linkl.AddLast(new Employee() { Name = "New", Age = 7, Mail = "New" });
                linkl.AddLast(new Employee() { Name = "New", Age = 7, Mail = "New" });
                linkl.AddLast(new Employee() { Name = "New", Age = 7, Mail = "New" });
                AddLinkedList(linkl);
                scroll.ScrollToVerticalOffset(scroll.ScrollableHeight / 10);
            }

        }

        private RelayCommand<object> _addToList;

        public ICommand AddToList
        {
            get { return _addToList ?? (_addToList = new RelayCommand<object>(obj => AddToListCommand(obj))); }
        }

        private void AddToListCommand(object o)
        {
            ScrollViewerElement.ScrollToBottom();
            LinkedList<Employee> secondList = new LinkedList<Employee>(MyList);
            
            foreach (Employee employee in secondList)
            {
                MyList.AddFirst(employee);
            }

            MyList = new LinkedList<Employee>(MyList);
        }

        private void AddLinkedList( LinkedList<Employee> linkedList)
        {
            foreach (Employee employee in linkedList)
            {
                MyList.AddFirst(employee);
            }

            MyList = new LinkedList<Employee>(MyList);
        }

        private void ScrollViewer_PreviewMouseWheel(object sender, MouseWheelEventArgs e)
        {
            ScrollViewer scrollviewer = sender as ScrollViewer;
            
            
                if (e.Delta > 0)    
                    scrollviewer.PageUp();
                else
                    scrollviewer.PageDown();
                e.Handled = true;
            
        }



        public event PropertyChangedEventHandler PropertyChanged;

        protected void NotifyPropertyChanged([CallerMemberName] String propertyName = "")
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
    }





    public class Employee
    {
        public string Name { get; set; }

        public int Age { get; set; }

        public string Mail { get; set; }
    }
}