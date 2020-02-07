using System;
using System.ComponentModel;
using System.Runtime.CompilerServices;

namespace ClientLourd.ViewModels
{
    /// Base on https://docs.microsoft.com/en-us/dotnet/api/system.componentmodel.inotifypropertychanged?view=netframework-4.8 implementation 
    /// <summary>
    /// Give access to NotifyPropertyChanged
    /// </summary>
    public abstract class ViewModelBase : INotifyPropertyChanged
    {
        public abstract void Init();
        public event PropertyChangedEventHandler PropertyChanged;

        protected void NotifyPropertyChanged([CallerMemberName] String propertyName = "")
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
    }
}