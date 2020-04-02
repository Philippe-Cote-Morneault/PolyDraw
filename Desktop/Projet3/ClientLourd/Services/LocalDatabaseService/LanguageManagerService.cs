using ClientLourd.Utilities.Enums;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Xml;

namespace ClientLourd.Services.LanguageManagerService
{
    public class LanguageManagerService
    {

        private ResourceDictionary _dict;
        private string _userID;
        public LanguageManagerService(string userID)
        {
            _userID = userID;
            _dict = new ResourceDictionary();
            _dict.Source = new Uri("..\\Resources\\UserSettings\\UserLanguage.xaml", UriKind.Relative);
        }

        public bool HasLanguageSet()
        {
            return _dict.Contains(_userID);
        }

        public string GetLanguage()
        {
            return _dict[_userID].ToString();
        }

        public void SetLanguage(string language)
        {
            _dict.Add(_userID, language);

            var settings = new XmlWriterSettings();
            settings.Indent = true;
            var writer = XmlWriter.Create(@"UserLanguage.xaml", settings);
            System.Windows.Markup.XamlWriter.Save(_dict, writer);
            
        }

    }
}
