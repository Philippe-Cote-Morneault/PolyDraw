using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Controls;

/*namespace ClientLourd.Utilities.ValidationRules
{
    class ChatInputBoxRule: ValidationRule
    {
        public ChatInputBoxRule()
        {

        }
        public override ValidationResult Validate(object value, CultureInfo cultureInfo)
        {
            string message = (string)value;
            
            if (message.Length == 5)
            {
                return new ValidationResult(false, "You have reached the maximum amount");
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
}*/