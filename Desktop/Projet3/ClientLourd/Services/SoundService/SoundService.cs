using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Media;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Timers;
using System.Windows;

namespace ClientLourd.Services.SoundService
{
    public class SoundService : INotifyPropertyChanged
    {
        private static Mutex mut;
        private SoundPlayer _soundPlayer;
        private System.Timers.Timer _spamController;
        private bool _canPlay;

        public SoundService()
        {
            SoundIsOn = true;
            _soundPlayer = new SoundPlayer();
            mut = new Mutex();
            _canPlay = true;
            _spamController = new System.Timers.Timer(500);
            _spamController.Elapsed += OnTimerElapsed;
            _spamController.AutoReset = true;
            _spamController.Start();
        }

        private void OnTimerElapsed(object sender, ElapsedEventArgs e)
        {
            _canPlay = true;
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
            if (_canPlay)
            {

                _canPlay = false;
                Task.Run(() =>
                {
                    if (SoundIsOn)
                    {
                        _soundPlayer.Stream = Properties.Resources.Message;
                        _soundPlayer.PlaySync();
                    }
                });
            }
        }

        public void PlayWordGuessedRight()
        {
            Task.Run(() =>
            {
                if (SoundIsOn)
                {
                    _soundPlayer.Stream = Properties.Resources.WordGuessedRight;
                    _soundPlayer.PlaySync();
                }
            });

        }
        public void PlayWordGuessedWrong()
        {
            if (_canPlay)
            {

                _canPlay = false;
                Task.Run(() =>
                {
                    if (SoundIsOn)
                    {
                        _soundPlayer.Stream = Properties.Resources.WordGuessedWrong;
                        _soundPlayer.PlaySync();
                    }
                });
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
