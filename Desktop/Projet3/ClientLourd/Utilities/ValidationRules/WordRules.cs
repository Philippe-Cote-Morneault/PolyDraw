using ClientLourd.Services.EnumService;
using ClientLourd.Utilities.Enums;
using ClientLourd.ViewModels;
using System.Windows;
using System;
using System.Globalization;
using System.Text.RegularExpressions;
using System.Windows.Controls;
using RestSharp;
using ClientLourd.Properties;

namespace ClientLourd.Utilities.ValidationRules
{
    public class WordRules: ValidationRule
    {
        public string Language
        {
            get
            {
                return (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel)?.SelectedLanguage;
            }
        }
        public override ValidationResult Validate(object value, CultureInfo cultureInfo)
        {

            string word = (string) value;
            if (word.Length > 20)
            {
                if (Language == Languages.EN.GetDescription())
                    return new ValidationResult(false, "Your word is to long");
                else
                    return new ValidationResult(false, "Votre mot est trop long");
            }

            if (string.IsNullOrWhiteSpace(word))
            {
                if (Language == Languages.EN.GetDescription())
                    return new ValidationResult(false, "The word can't be empty");
                else
                    return new ValidationResult(false, "The word can't be empty");
            }
            Regex spaceRegex = new Regex(@"\s");
            if (spaceRegex.IsMatch(word))
            {
                return new ValidationResult(false, "White space character are not allowed");
            }
            
            return new ValidationResult(true,"");
        }
    }
}