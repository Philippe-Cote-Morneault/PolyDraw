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
        public UsernameRule()
        {
        }

        public override ValidationResult Validate(object value, CultureInfo cultureInfo)
        {
            string username = (string) value;
            LoginInputRules loginInputValidator = new LoginInputRules();

            if (loginInputValidator.StringIsEmpty(username))
            {
                return new ValidationResult(false, "The username cannot be empty.");
            }

            if (!loginInputValidator.IsAlphaNumeric(username))
            {
                return new ValidationResult(false, "The username must only contain alphanumeric characters.");
            }

            if (!loginInputValidator.LengthIsOk(username))
            {
                return new ValidationResult(false, "The username must be between 4 and 12 characters.");
            }

            return ValidationResult.ValidResult;
        }
    }
}