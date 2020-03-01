using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Media;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading.Tasks;

namespace ClientLourd.Services.SoundService
{
    public class SoundService: INotifyPropertyChanged
    {
        public SoundService()
        {
            SoundIsOn = true;
        }

        bool _soundIsOn;
        public bool SoundIsOn
        {
            get => _soundIsOn;
            set
            {
                if (value != _soundIsOn) 
                {
                    _soundIsOn = value;
                    NotifyPropertyChanged();
                }
            }
        }

        public void ToggleSound()
        {
            SoundIsOn = !SoundIsOn;
        }

        public void PlayNotification()
        {
            if (SoundIsOn)
            {
                new SoundPlayer(Properties.Resources.Message).Play();
            }
        }

        public void PlayWordGuessedRight() 
        {
            //TODO
        }
        public void PlayWordGuessedWrong()
        {
            //TODO
        }


        public event PropertyChangedEventHandler PropertyChanged;

        protected void NotifyPropertyChanged([CallerMemberName] String propertyName = "")
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
    }
}
