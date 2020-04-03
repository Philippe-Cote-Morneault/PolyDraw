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
        private ResourceDictionary _tutoDict;

        private string _userID;
        public UserSettingsManagerService(string userID)
        {
            _userID = userID;
            
            if (!File.Exists($"{Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData)}\\UserLanguages.xaml"))
            {
                CreateFile($"{Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData)}\\UserLanguages.xaml");
            }

            if (!File.Exists($"{Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData)}\\UserTutorialSeen.xaml"))
            {
                CreateFile($"{Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData)}\\UserTutorialSeen.xaml");
            }


            _langDict = new ResourceDictionary();
            _langDict.Source = new Uri($"{Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData)}\\UserLanguages.xaml", UriKind.RelativeOrAbsolute);

            _tutoDict = new ResourceDictionary();
            _tutoDict.Source = new Uri($"{Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData)}\\UserTutorialSeen.xaml", UriKind.RelativeOrAbsolute);

        }

        public void CreateFile(string path)
        {  
            var settings = new XmlWriterSettings();
            settings.Indent = true;
            using (XmlWriter w = XmlWriter.Create(path, settings))
            {
                XamlWriter.Save(new ResourceDictionary(), w);
            }
        }

        public void OverwriteFile(bool IsLanguageDict)
        {
            var dict = (IsLanguageDict) ? _langDict : _tutoDict;
            var filename = (IsLanguageDict) ? "UserLanguages.xaml" : "UserTutorialSeen.xaml";
            var settings = new XmlWriterSettings();
            settings.Indent = true;
            using (XmlWriter w = XmlWriter.Create($"{Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData)}\\{filename}", settings))
            {
                XamlWriter.Save(dict, w);
            }
        }

        public string GetLanguage()
        {
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

            OverwriteFile(true);
        }

        public bool HasSeenTutorial() 
        {
            if (!_tutoDict.Contains(_userID))
            {
                _tutoDict.Add(_userID, "True");
                OverwriteFile(false);
                
                return false;
            }

            return true;
        }

    }
}
