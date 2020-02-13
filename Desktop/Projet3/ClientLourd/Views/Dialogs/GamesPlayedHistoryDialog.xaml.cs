﻿using ClientLourd.ViewModels;
using MaterialDesignThemes.Wpf;
using System;
using System.Collections.Generic;
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
using System.Windows.Navigation;
using System.Windows.Shapes;

namespace ClientLourd.Views.Dialogs
{
    /// <summary>
    /// Interaction logic for GamesPlayedHistoryDialog.xaml
    /// </summary>
    public partial class GamesPlayedHistoryDialog : UserControl
    {
        public GamesPlayedHistoryDialog()
        {
            InitializeComponent();
            (((MainWindow)Application.Current.MainWindow).MainWindowDialogHost as DialogHost).CloseOnClickAway = true;
        }
    }
}
