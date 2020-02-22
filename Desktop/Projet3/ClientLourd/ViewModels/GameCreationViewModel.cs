using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Windows.Documents;
using ClientLourd.Services.EnumService;
using ClientLourd.Utilities.Enums;

namespace ClientLourd.ViewModels
{
    public class GameCreationViewModel : ViewModelBase
    {
        public GameCreationViewModel()
        {
            Hints = new ObservableCollection<string>(new string[3]);
            Hints.CollectionChanged += (sender, args) => { NotifyPropertyChanged(nameof(AreFieldsEmpty)); }; 
            NotifyPropertyChanged(nameof(SelectedMode));
        }
        
        public override void AfterLogOut() { throw new System.NotImplementedException(); }
        public override void AfterLogin() { throw new System.NotImplementedException(); }

        private ObservableCollection<string> _hints;
        public ObservableCollection<string> Hints 
        {
            get { return _hints; }
            set
            {
                if (_hints != value)
                {
                    _hints = value;
                    NotifyPropertyChanged();
                    NotifyPropertyChanged(nameof(AreFieldsEmpty));
                }
            }
        }
        public bool AreFieldsEmpty
        {
            get { return string.IsNullOrEmpty(_word) || _hints.Any(string.IsNullOrEmpty); }
        }
        public string Word 
        {
            get { return _word; }
            set
            {
                if (_word != value)
                {
                    _word = value;
                    NotifyPropertyChanged();
                    NotifyPropertyChanged(nameof(AreFieldsEmpty));
                }
            }
        }
        private string _word;

        public string SelectedMode 
        {
            get { return _selectedMode.GetDescription(); }
            set { _selectedMode = value.GetEnumFromDescription<PotraceMode>(); } 
        }
        private PotraceMode _selectedMode;

        public List<string> PotraceModes
        {
            get { return EnumManager.GetAllDescriptions<PotraceMode>(); }
        }  
    }
}