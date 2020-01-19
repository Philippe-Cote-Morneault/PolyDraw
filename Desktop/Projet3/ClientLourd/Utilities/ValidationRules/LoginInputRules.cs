using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ClientLourd.Utilities.ValidationRules
{
    class LoginInputRules
    {
        public LoginInputRules()
        {

        }

        public bool stringIsEmpty(string myInput)
        {
            return String.IsNullOrWhiteSpace(myInput);
        }

        public bool LengthIsOk(string myInput)
        {
            return myInput.Length >= 4 && myInput.Length <= 12;
        }
    }
}
