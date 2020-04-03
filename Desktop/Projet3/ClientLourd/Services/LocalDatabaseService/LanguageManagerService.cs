using ClientLourd.Utilities.Enums;
using System;
using System.Collections.Generic;
using System.Configuration;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Markup;
using System.Xml;

namespace ClientLourd.Services.UserManagerService
{
    public class UserSettingsManagerService
    {

        private ResourceDictionary _dict;
        private string _userID;
        public UserSettingsManagerService(string userID)
        {
            _userID = userID;
            _dict = new ResourceDictionary(); 
            _dict.Source = new Uri("..\\Resources\\UserSettings\\UserSettings.xaml", UriKind.Relative);
        }


        public string GetLanguage()
        {
            //return _dict["Laguage"].ToString();
            return (_dict.Contains(_userID)) ? _dict[_userID].ToString(): "System"; 
        }


        public void SetLanguage(string language)
        {
            if (!_dict.Contains(_userID))
            {
                _dict.Add(_userID, language);
            }
            else
            {
                _dict[_userID] = language;
            }

            var settings = new XmlWriterSettings();
            settings.Indent = true;
            var writer = XmlWriter.Create($"{AppDomain.CurrentDomain.BaseDirectory}..\\..\\Resources\\UserSettings\\UserSettings.xaml", settings);
            XamlWriter.Save(_dict, writer);
        }

    }
}
