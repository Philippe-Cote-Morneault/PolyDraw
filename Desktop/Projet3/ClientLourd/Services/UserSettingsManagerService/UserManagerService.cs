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

namespace ClientLourd.Services.UserSettingsManagerService
{
    public class UserSettingsManagerService
    {

        private ResourceDictionary _langDict;
        private string _userID;
        public UserSettingsManagerService(string userID)
        {
            _userID = userID;
            _langDict = new ResourceDictionary();
            _langDict.Source = new Uri("..\\Resources\\UserSettings\\UserLanguages.xaml", UriKind.Relative);
        }


        public string GetLanguage()
        {
            //return _dict["Laguage"].ToString();
            return (_langDict.Contains(_userID)) ? _langDict[_userID].ToString() : "System";
        }


        public void SetLanguage(string language)
        {
            if (!_langDict.Contains(_userID))
            {
                _langDict.Add(_userID, language);
            }
            else
            {
                _langDict[_userID] = language;
            }

            var settings = new XmlWriterSettings();
            settings.Indent = true;
            var writer = XmlWriter.Create($"{AppDomain.CurrentDomain.BaseDirectory}..\\..\\Resources\\UserSettings\\UserLanguages.xaml", settings);
            XamlWriter.Save(_langDict, writer);
        }

    }
}
