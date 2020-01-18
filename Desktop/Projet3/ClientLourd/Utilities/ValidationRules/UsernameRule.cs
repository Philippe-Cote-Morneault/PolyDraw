using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Controls;

namespace ClientLourd.Utilities.ValidationRules
{
    class UsernameRule: ValidationRule
    {
        public UsernameRule()
        {

        }

        public override ValidationResult Validate(object value, CultureInfo cultureInfo)
        {
            string username = (string)value;

            if (String.IsNullOrWhiteSpace(username))
            {
                return new ValidationResult(false, "The username cannot be empty.");
            }

            if (username.Length < 5 || username.Length > 12)
            { 
                return new ValidationResult(false, "The username must be between 5 and 12 characters.");
            }

            return ValidationResult.ValidResult; 
        }

    }
}
