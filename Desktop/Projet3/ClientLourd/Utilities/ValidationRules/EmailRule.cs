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
    class EmailRule: ValidationRule
    {
        public EmailRule()
        {

        }

        public override ValidationResult Validate(object value, CultureInfo cultureInfo)
        {
            string email = (string)value;
            Regex r = new Regex("^[a-zA-Z0-9]*$");


            if (!new Regex(@"^[\w!#$%&'*+\-/=?\^_`{|}~]+(\.[\w!#$%&'*+\-/=?\^_`{|}~]+)*@((([\-\w]+\.)+[a-zA-Z]{2,4})|(([0-9]{1,3}\.){3}[0-9]{1,3}))\z").IsMatch(email))
            {
                return new ValidationResult(false, "The please enter a valid email address.");
            }

            return ValidationResult.ValidResult;
        }
    }
}
