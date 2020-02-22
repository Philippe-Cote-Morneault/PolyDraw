using System.Globalization;
using System.Windows.Controls;

namespace ClientLourd.Utilities.ValidationRules
{
    public class HintRules : ValidationRule
    {
        public override ValidationResult Validate(object value, CultureInfo cultureInfo)
        {
            string hint = (string) value;
            if (hint.Length > 40)
            {
                return new ValidationResult(false, "Your hint is to long");
            }

            if (string.IsNullOrWhiteSpace(hint))
            {
                return new ValidationResult(false, "The hint can't be empty");
            }
            
            return new ValidationResult(true, "");
        }
    }
}