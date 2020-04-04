using ClientLourd.Services.EnumService;
using ClientLourd.Utilities.Enums;
using ClientLourd.ViewModels;
using System.Windows;
using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using System.Windows.Controls;

namespace ClientLourd.Utilities.ValidationRules
{
    class LobbyNameRule : ValidationRule
    {
        public string Language
        {
            get
            {
                return (((MainWindow) Application.Current.MainWindow)?.DataContext as MainViewModel)?.SelectedLanguage;
            }
        }

        public static bool IsAlphaNumerical(string lobbyName)
        {
            return lobbyName == null || new Regex("^[a-zA-Z0-9]*$").IsMatch(lobbyName);
        }

        public override ValidationResult Validate(object value, CultureInfo cultureInfo)
        {
            string lobbyName = (string) value;

            if (!IsAlphaNumerical(lobbyName))
            {
                if (Language == Languages.EN.GetDescription())
                    return new ValidationResult(false, "The lobby name must be alphanumeric.");
                else
                    return new ValidationResult(false,
                        "Le nom de la salle d'attente ne doit contenir que des caractères alphanumériques.");
            }

            return ValidationResult.ValidResult;
        }
    }
}