using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Media;
using ClientLourd.Models.NonBindable;
using MaterialDesignThemes.Wpf;

namespace ClientLourd.Services.DialogService
{
    public class DialogManager
    {
        private DialogHost _host;
        private Mutex _mutex;

        public DialogManager(DialogHost host)
        {
            _host = host;
            _mutex = new Mutex();
        }

        public async Task<object> Show(DialogRequest request)
        {
                _mutex.WaitOne();
                _host.CloseOnClickAway = request.CloseOnClickAway;
                return DialogHost.Show(request.Dialog, (object sender , DialogClosingEventArgs args) =>
                {
                    _host.CloseOnClickAway = false;
                    _mutex.ReleaseMutex();
                });
        }
        
        


    }
}