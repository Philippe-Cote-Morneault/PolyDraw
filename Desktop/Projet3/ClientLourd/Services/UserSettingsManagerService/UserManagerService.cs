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

        private readonly static string UserLanguagesPath = $"{Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData)}\\UserLanguages.xaml";
        private readonly static string UserTutorialPath = $"{Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData)}\\UserTutorialSeen.xaml";


        private string _userID;
        public UserSettingsManagerService(string userID)
        {
            _userID = userID;
            
            if (!File.Exists(UserLanguagesPath))
            {
                CreateFile(UserLanguagesPath);
            }

            if (!File.Exists(UserTutorialPath))
            {
                CreateFile(UserTutorialPath);
            }


            _langDict = new ResourceDictionary();
            _langDict.Source = new Uri(UserLanguagesPath, UriKind.RelativeOrAbsolute);

            _tutoDict = new ResourceDictionary();
            _tutoDict.Source = new Uri(UserTutorialPath, UriKind.RelativeOrAbsolute);

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
            var path = (IsLanguageDict) ? UserLanguagesPath : UserTutorialPath;
            var settings = new XmlWriterSettings();
            settings.Indent = true;
            using (XmlWriter w = XmlWriter.Create(path, settings))
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
