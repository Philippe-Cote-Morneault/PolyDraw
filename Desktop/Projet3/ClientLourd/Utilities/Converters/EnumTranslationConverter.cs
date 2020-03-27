using ClientLourd.Services.EnumService;
using ClientLourd.Utilities.Enums;
using ClientLourd.ViewModels;
using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Data;

namespace ClientLourd.Utilities.Converters
{
    class EnumTranslationConverter : IValueConverter
    {
        public string Language
        {
            get
            {
                return (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel)?.SelectedLanguage;
            }
        }

        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            string enumStr = value.ToString();
            if (Language == Languages.FR.GetDescription())
            {
                if (enumStr == GameModes.FFA.ToString() || enumStr == GameModes.FFA.GetDescription())
                    return "Mêlée générale";

                if (enumStr == GameModes.Coop.ToString() || enumStr == GameModes.Coop.GetDescription())
                    return "Coopératif";

                if (enumStr == GameModes.Solo.ToString() || enumStr == GameModes.Solo.GetDescription())
                    return "Solo";


                if (enumStr == DifficultyLevel.Easy.ToString() || enumStr == DifficultyLevel.Easy.GetDescription())
                    return "Facile";

                if (enumStr == DifficultyLevel.Medium.ToString() || enumStr == DifficultyLevel.Medium.GetDescription())
                    return "Moyen";

                if (enumStr == DifficultyLevel.Hard.ToString() || enumStr == DifficultyLevel.Hard.GetDescription())
                    return "Difficile";
            }
            return value;
        }



        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}
