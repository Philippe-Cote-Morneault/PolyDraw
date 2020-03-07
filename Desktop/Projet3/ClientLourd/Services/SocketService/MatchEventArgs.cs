﻿using System;

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
        public int Points
        {
            get  => _data["Points"]; 
        } 
        public int PointsTotal
        {
            get  => _data["PointsTotal"]; 
        } 
        public int Laps
        {
            get  =>_data["Laps"]; 
        } 
        public string GameTime
        {
            get  => _data["GameTime"]; 
        } 
        public bool Valid
        {
            get  => _data["Valid"]; 
        }

        public int Type
        {
            get => _data["Type"];
        }

        public string WinnerID
        {
            get => _data["Winner"];
        }
        
    }
}