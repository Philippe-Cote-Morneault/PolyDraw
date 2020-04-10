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
                return (((MainWindow) Application.Current.MainWindow)?.DataContext as MainViewModel)?.SelectedLanguage;
            }
        }

        // Poopie code
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            string enumStr = value.ToString();
            if (enumStr == "FFA" || enumStr == "Mêlée générale")
            {
                var x = 1;
            }
            if (Language == Languages.FR.GetDescription())
            {
                //langue 
                if (enumStr == Languages.EN.ToString() || enumStr == Languages.EN.GetDescription())
                    return "Anglais";
                if (enumStr == Languages.FR.ToString() || enumStr == Languages.FR.GetDescription())
                    return "Français";

                // Mode de jeu
                if (enumStr == GameModes.FFA.ToString() || enumStr == GameModes.FFA.GetDescription())
                    return "Mêlée générale";

                if (enumStr == GameModes.Coop.ToString() || enumStr == GameModes.Coop.GetDescription())
                    return "Coopératif";

                if (enumStr == GameModes.Solo.ToString() || enumStr == GameModes.Solo.GetDescription())
                    return "Solo";


                // Difficulties
                if (enumStr == DifficultyLevel.Easy.ToString() || enumStr == DifficultyLevel.Easy.GetDescription())
                    return "Facile";

                if (enumStr == DifficultyLevel.Medium.ToString() || enumStr == DifficultyLevel.Medium.GetDescription())
                    return "Moyen";

                if (enumStr == DifficultyLevel.Hard.ToString() || enumStr == DifficultyLevel.Hard.GetDescription())
                    return "Difficile";


                // Potrace modes
                if (enumStr == PotraceMode.Classic.ToString() || enumStr == PotraceMode.Classic.GetDescription())
                    return "Mode classique (ordre de dessin)";

                if (enumStr == PotraceMode.Random.ToString() || enumStr == PotraceMode.Random.GetDescription())
                    return "Mode aléatoire";

                if (enumStr == PotraceMode.RightToLeft.ToString() ||
                    enumStr == PotraceMode.RightToLeft.GetDescription())
                    return "De droite à gauche";

                if (enumStr == PotraceMode.LeftToRight.ToString() ||
                    enumStr == PotraceMode.LeftToRight.GetDescription())
                    return "De gauche à droite";

                if (enumStr == PotraceMode.TopToBottom.ToString() ||
                    enumStr == PotraceMode.TopToBottom.GetDescription())
                    return "Du haut vers le bas";

                if (enumStr == PotraceMode.BottomToTop.ToString() ||
                    enumStr == PotraceMode.BottomToTop.GetDescription())
                    return "Du bas vers le haut";

                if (enumStr == PotraceMode.InsideToOutside.ToString() ||
                    enumStr == PotraceMode.InsideToOutside.GetDescription())
                    return "De l'intérieur vers l'extérieur";

                if (enumStr == PotraceMode.OutsideToInside.ToString() ||
                    enumStr == PotraceMode.OutsideToInside.GetDescription())
                    return "De l'extérieur vers l'intérieur";
            }

            return value;
        }


        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}