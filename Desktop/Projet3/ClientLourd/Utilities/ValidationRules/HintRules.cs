using ClientLourd.Services.EnumService;
using ClientLourd.Utilities.Enums;
using ClientLourd.ViewModels;
using System.Windows;

using System.Globalization;
using System.Windows.Controls;

namespace ClientLourd.Utilities.ValidationRules
{
    public class HintRules : ValidationRule
    {
        public string Language
        {
            get
            {
                return (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel)?.SelectedLanguage;
            }
        }
        public override ValidationResult Validate(object value, CultureInfo cultureInfo)
        {
            string hint = (string) value;
            if (hint.Length > 40)
            {
                if (Language == Languages.EN.GetDescription())
                    return new ValidationResult(false, "Your hint is too long");
                else
                    return new ValidationResult(false, "Votre indice est trop longue");

            }

            if (string.IsNullOrWhiteSpace(hint))
            {
                if (Language == Languages.EN.GetDescription())
                    return new ValidationResult(false, "The hint can't be empty");
                else
                    return new ValidationResult(false, "L'indice ne peut pas être vide");

            }

            return new ValidationResult(true, "");
        }
    }
}