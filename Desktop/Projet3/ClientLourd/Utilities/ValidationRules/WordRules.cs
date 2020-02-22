using System;
using System.Globalization;
using System.Text.RegularExpressions;
using System.Windows.Controls;
using RestSharp;

namespace ClientLourd.Utilities.ValidationRules
{
    public class WordRules: ValidationRule
    {
        public override ValidationResult Validate(object value, CultureInfo cultureInfo)
        {
            string word = (string) value;
            if (word.Length > 20)
            {
                return new ValidationResult(false, "Your word is to long");
            }

            if (string.IsNullOrWhiteSpace(word))
            {
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