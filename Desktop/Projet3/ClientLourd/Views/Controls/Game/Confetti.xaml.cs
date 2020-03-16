using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;

namespace ClientLourd.Views.Controls.Game
{
    /// <summary>
    /// Interaction logic for Confetti.xaml
    /// </summary>
    public partial class Confetti : UserControl
    {
        public Confetti()
        {
            InitializeComponent();
            SetColor();
        }

        private void SetColor()
        {
            Random random = new Random();
            int color = random.Next(1, 5);

            switch (color)
            {
                case 1:
                    Path.Fill = Brushes.Red;
                    break;
                case 2:
                    Path.Fill = Brushes.Yellow;
                    break;
                case 3:
                    Path.Fill = Brushes.LimeGreen;
                    break;
                case 4:
                    Path.Fill = Brushes.Blue;
                    break;
                default:
                    throw new Exception("No confetti color associated to number");
            }
        }
    }
}
