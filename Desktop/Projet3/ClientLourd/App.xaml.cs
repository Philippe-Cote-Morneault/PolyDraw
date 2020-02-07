using System;
using System.Collections.Generic;
using System.Configuration;
using System.Data;
using System.Linq;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Threading;
using ClientLourd.Models;
using ClientLourd.Views.Dialogs;
using MaterialDesignThemes.Wpf;

namespace ClientLourd
{
    /// <summary>
    /// Interaction logic for App.xaml
    /// </summary>
    public partial class App : Application
    {
        private async void App_OnDispatcherUnhandledException(object sender, DispatcherUnhandledExceptionEventArgs e)
        {
            e.Handled = true;
            try
            {
                await DialogHost.Show(new ClosableErrorDialog($"{e.Exception.Message} The application will close."));
            }
            catch
            {
                MessageBox.Show($"{e.Exception.Message} The application will close.", String.Empty, MessageBoxButton.OK,
                    MessageBoxImage.Error);
            }

            Current.Shutdown();
        }
    }
}