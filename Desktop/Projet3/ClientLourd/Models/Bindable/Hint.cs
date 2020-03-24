using System;

namespace ClientLourd.Models.Bindable
{
    public class Hint : ModelBase
    {
        public Hint()
        {
            Text = "";
        }
        private string _text;

        public string Text
        {
            get => _text;
            set
            {
                if (_text != value)
                {
                    _text = value;
                    NotifyPropertyChanged();
                    NotifyPropertyChanged(nameof(IsValid));
                }
            }
        }

        private bool _isSelected;
        public bool IsSelected
        {
            get => _isSelected;
            set
            {
                if (_isSelected != value)
                {
                    _isSelected = value;
                    NotifyPropertyChanged(nameof(IsValid));
                    NotifyPropertyChanged(nameof(Text));
                }
            }
        }

        public bool IsValid
        {
            get => !String.IsNullOrWhiteSpace(Text);
        }
    }
}