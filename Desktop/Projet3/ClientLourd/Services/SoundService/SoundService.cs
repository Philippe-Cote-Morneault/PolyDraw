using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Media;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows;

namespace ClientLourd.Services.SoundService
{
    public class SoundService : INotifyPropertyChanged
    {
        private static Mutex mut;

        public SoundService()
        {
            SoundIsOn = true;
            mut = new Mutex();
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
            //mut.WaitOne();
            if (SoundIsOn)
            {
                new SoundPlayer(Properties.Resources.Message).Play();
                
            }
            //mut.ReleaseMutex();
        }

        public void PlayWordGuessedRight()
        {
            if (SoundIsOn)
            {
                new SoundPlayer(Properties.Resources.WordGuessedRight).Play();
            }
        }
        public void PlayWordGuessedWrong()
        {
            if (SoundIsOn)
            {
                new SoundPlayer(Properties.Resources.WordGuessedWrong).Play();
            }
        }

        public void PlayTimerWarning()
        {
            if (SoundIsOn)
            {
                new SoundPlayer(Properties.Resources.TimerWarning).Play();
            }
        }

        private void DisableSpam()
        {
            _soundIsOn = false;

            Task.Delay(1000).ContinueWith(_ =>
            {
                Application.Current.Dispatcher.Invoke(new Action(() =>
                {
                    _soundIsOn = true;
                }));
            });

        }


        public event PropertyChangedEventHandler PropertyChanged;

        protected void NotifyPropertyChanged([CallerMemberName] String propertyName = "")
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
    }
}
