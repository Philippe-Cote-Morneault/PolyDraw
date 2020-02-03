using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Text.RegularExpressions;


namespace ClientLourd.Utilities.ValidationRules
{
    class LoginInputRules
    {
        public LoginInputRules()
        {
        }
        
        public bool StringIsEmpty(string myInput)
        {
            return String.IsNullOrEmpty(myInput);
        }
        public bool StringIsWhiteSpace(string myInput)
        {
            return String.IsNullOrWhiteSpace(myInput);
        }

        public bool UsernameLengthIsOk(string myInput)
        {
            return myInput.Length >= 4 && myInput.Length <= 12;
        }

        public bool PasswordLengthIsOk(string myInput)
        {
            return myInput.Length >= 4 && myInput.Length <= 64;
        }
        
        public bool IsAlphaNumeric(string myInput)
        {
            Regex r = new Regex("^[a-zA-Z0-9]*$");

            return r.IsMatch(myInput);
        }
    }
}