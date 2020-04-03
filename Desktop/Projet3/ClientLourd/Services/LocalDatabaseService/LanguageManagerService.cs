using ClientLourd.Utilities.Enums;
using System;
using System.Collections.Generic;
using System.Configuration;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Markup;
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


        public void CreateUserLanguageFile()
        {
            var tmpDict = new ResourceDictionary();
            tmpDict.Source = new Uri("..\\Resources\\UserSettings\\UserLanguage.xaml", UriKind.Relative);
            StreamWriter writer = new StreamWriter($"{Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData)}\\UserLanguage.xaml");
            XamlWriter.Save(tmpDict, writer);
            writer.Close();
        }

        public void SetLanguage(string language)
        {
            _dict.Add(_userID, language);
            Application.Current.Dispatcher.Invoke(() => 
            {
                var x = Properties.Settings.Default.Properties;
                Properties.Settings.Default.Properties.Add(new SettingsProperty(_userID));
                Properties.Settings.Default.Save();
            });
            var y = Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData);

//            Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "\\PathToCFG");

            //StreamWriter writer = new StreamWriter($"{Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData)}\\UserLanguage.xaml");
            //StreamWriter writer = new StreamWriter($"{AppDomain.CurrentDomain.BaseDirectory}..\\..\\Resources\\UserSettings\\UserLanguage.xaml");

            
            //XamlWriter.Save(_dict, writer);
            //writer.Close();

            var settings = new XmlWriterSettings();
            settings.Indent = true;
            var writer = XmlWriter.Create($"{AppDomain.CurrentDomain.BaseDirectory}..\\..\\Resources\\UserSettings\\UserLanguage.xaml", settings);
            //System.Windows.Markup.XamlWriter.Save(_dict, writer);

            XamlWriter.Save(_dict, writer);
            

        }

    }
}
