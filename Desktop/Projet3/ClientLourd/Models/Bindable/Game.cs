using System;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;

namespace ClientLourd.Models.Bindable
{
    public class Game: ModelBase
    {
        private ObservableCollection<Hint> _hints;
        private string _word;
        public Game()
        {
            Word = "";
            Hints = new ObservableCollection<Hint>()
            {
                new Hint(),
                new Hint(),
                new Hint(),
                new Hint(),
                new Hint(),
                new Hint(),
                new Hint(),
                new Hint(),
                new Hint(),
                new Hint(),
            };
        }

        public bool IsInformationInvalid
        {
            get { return string.IsNullOrWhiteSpace(Word) || Hints.Where(h => h.IsSelected).Any(h => !h.IsValid); }
        }
        public string Word
        {
            get => _word;
            set
            {
                if (value != _word)
                {
                    _word = value;
                    NotifyPropertyChanged();
                    NotifyPropertyChanged(nameof(IsInformationInvalid));
                }
            }
        }
        public ObservableCollection<Hint> Hints
        {
            get => _hints;
            set
            {
                if (value != _hints)
                {
                    _hints = value;
                    foreach (var hint in _hints)
                    {
                        hint.PropertyChanged += delegate(object sender, PropertyChangedEventArgs args)
                        {
                            if (args.PropertyName == nameof(hint.Text))
                            {
                                NotifyPropertyChanged(nameof(IsInformationInvalid));
                            }
                        };
                    }
                    NotifyPropertyChanged(nameof(IsInformationInvalid));
                    NotifyPropertyChanged();
                }
            }
        }
        

    }
}