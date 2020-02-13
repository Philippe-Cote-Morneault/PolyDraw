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
        public override ValidationResult Validate(object value, CultureInfo cultureInfo)
        {
            string name = (string)value;


            if (String.IsNullOrWhiteSpace(name))
            {
                return new ValidationResult(false, "This field cannot be empty.");
            }

            return ValidationResult.ValidResult;
        }
    }
}
