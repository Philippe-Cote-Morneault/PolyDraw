using ClientLourd.Services.EnumService;
using ClientLourd.Utilities.Enums;
using ClientLourd.ViewModels;
using System.Windows;
using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Controls;

namespace ClientLourd.Utilities.ValidationRules
{
    class NameRule : ValidationRule
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
            string name = (string)value;


            if (String.IsNullOrWhiteSpace(name))
            {
                if (Language == Languages.EN.GetDescription())
                    return new ValidationResult(false, "This field cannot be empty.");
                else
                    return new ValidationResult(false, "Le champ ne peut pas être vide.");
            }

            return ValidationResult.ValidResult;
        }
    }
}
