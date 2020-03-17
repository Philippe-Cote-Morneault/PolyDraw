using System;
using System.Collections.Generic;
using ClientLourd.Models.Bindable;

namespace ClientLourd.Services.SocketService
{
    public class MatchEventArgs : EventArgs
    {
        
        public MatchEventArgs(dynamic data)
        {
            _data = data;
        }

        private dynamic _data;

        public string UserID
        {
            get  => _data["UserID"]; 
        } 
        
        public string Username
        {
            get  => _data["Username"]; 
        } 
        
        public DateTime Time
        {
            get  => DateTime.MinValue.AddMilliseconds(_data["Time"]); 
        } 
        public string DrawingID
        {
            get  => _data["DrawingID"]; 
        } 
        public string Word
        {
            get  => _data["Word"]; 
        } 
        public string TimesUpType
        {
            get  => _data["Type"]; 
        } 
        public long Points
        {
            get  => _data["Points"]; 
        } 
        public long PointsTotal
        {
            get  => _data["PointsTotal"]; 
        } 
        public long Laps
        {
            get  =>_data["Laps"]; 
        }

        public long LapTotal
        {
            get => _data["LapTotal"];
        }
        public string GameTime
        {
            get  => _data["GameTime"]; 
        } 
        public bool Valid
        {
            get  => _data["Valid"]; 
        }

        public long Type
        {
            get => _data["Type"];
        }

        public string WinnerID
        {
            get => _data["Winner"];
        }
        
        public long WordLength
        {
            get => _data["Length"];
        }

        public Object[] Players
        {
            get => _data["Players"];
        }

        public string WinnerName
        {
            get => _data["WinnerName"];
        }

        public bool HasHint
        {
            get => ((Dictionary<string,string>)_data).ContainsKey("Hint");
        }
        public string Hint
        {
            get => _data["Hint"];
        }
        
        public string Error
        {
            get => _data["Error"];
        }
        
    }
}