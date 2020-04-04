using System;
using System.ComponentModel;
using System.Windows.Controls;
using System.Windows.Ink;
using System.Windows.Input;
using System.Windows.Media;
using ClientLourd.Models.Bindable;
using ClientLourd.Utilities.Commands;

namespace ClientLourd.ViewModels
{
    class EditorViewModel : ViewModelBase
    {
        private EditorInformation _editorInformation = new EditorInformation();

        public EditorInformation EditorInformation
        {
            get { return _editorInformation; }
            set
            {
                _editorInformation = value;
                NotifyPropertyChanged();
            }
        }


        public EditorViewModel()
        {
        }

        private RelayCommand<InkCanvasEditingMode> _changeToolCommand;

        public ICommand ChangeToolCommand
        {
            get
            {
                return _changeToolCommand ??
                       (_changeToolCommand = new RelayCommand<InkCanvasEditingMode>(tool =>
                       {
                           EditorInformation.SelectedTool = tool;
                       }));
            }
        }

        RelayCommand<StylusTip> _changeTipCommand;

        public ICommand ChangeTipCommand
        {
            get
            {
                return _changeTipCommand ??
                       (_changeTipCommand = new RelayCommand<StylusTip>(tip =>
                       {
                           EditorInformation.SelectedTip = tip;
                       }));
            }
        }

        RelayCommand<Color> _changeColorCommand;

        public ICommand ChangeColorCommand
        {
            get
            {
                return _changeColorCommand ??
                       (_changeColorCommand = new RelayCommand<Color>(color =>
                       {
                           EditorInformation.SelectedColor = color;
                           OnSelectPen();
                       }));
            }
        }

        public override void AfterLogOut()
        {
            //TODO 
        }

        public override void AfterLogin()
        {
            //TODO 
        }

        public delegate void EditorEventHandler(object sender, EventArgs args);

        public event EditorEventHandler SelectPen;

        protected virtual void OnSelectPen()
        {
            SelectPen?.Invoke(this, EventArgs.Empty);
        }
    }
}