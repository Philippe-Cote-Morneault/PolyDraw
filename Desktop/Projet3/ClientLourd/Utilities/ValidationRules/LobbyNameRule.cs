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
    class LobbyNameRule: ValidationRule
    {

        public static bool IsAlphaNumerical(string lobbyName)
        {
            return lobbyName == null || new Regex("^[a-zA-Z0-9]*$").IsMatch(lobbyName);
        }

        public override ValidationResult Validate(object value, CultureInfo cultureInfo)
        {
            string lobbyName = (string)value;

            if (!IsAlphaNumerical(lobbyName))
            {
                return new ValidationResult(false, "The lobby name must be alphanumeric.");
            }

            return ValidationResult.ValidResult;
        }
    }
}
