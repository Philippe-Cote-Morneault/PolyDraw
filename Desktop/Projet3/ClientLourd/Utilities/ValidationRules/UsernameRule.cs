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
    class UsernameRule : ValidationRule
    {

        public string Language
        {
            get
            {
                return (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel)?.SelectedLanguage;
            }
        }


        public UsernameRule()
        {
        }

        public override ValidationResult Validate(object value, CultureInfo cultureInfo)
        {
            string username = (string) value;
            LoginInputRules loginInputValidator = new LoginInputRules();

            if (loginInputValidator.StringIsEmpty(username))
            {
                if (Language == Languages.EN.GetDescription())
                    return new ValidationResult(false, "The username cannot be empty");
                return new ValidationResult(false, "¨Le nom d'utilisateur ne peut pas être vide.");
            }

            if (loginInputValidator.StringIsWhiteSpace(username))
            {
                if (Language == Languages.EN.GetDescription())
                    return new ValidationResult(false, "The username cannot be empty.");
                return new ValidationResult(false, "¨Le nom d'utilisateur ne peut pas être vide.");

            }

            if (!loginInputValidator.IsAlphaNumeric(username))
            {
                if (Language == Languages.EN.GetDescription())
                    return new ValidationResult(false, "The username must only contain alphanumeric characters.");
                return new ValidationResult(false, "Le nom d'utilisateur ne peut contenir que des caractères alphanumériques.");
            }

            if (!loginInputValidator.UsernameLengthIsOk(username))
            {
                if (Language == Languages.EN.GetDescription())
                    return new ValidationResult(false, "The username must be between 4 and 12 characters.");
                return new ValidationResult(false, "Le nom d'utilisateur doit avoir une longueur entre 4 et 12 caractères.");
            }

            return ValidationResult.ValidResult;
        }
    }
}