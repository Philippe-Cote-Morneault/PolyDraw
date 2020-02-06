using System.Windows.Ink;

namespace ClientLourd.Models.Bindable
{
    class Editor : ModelBase
    {
        public StrokeCollection traits = new StrokeCollection();
        private StrokeCollection traitsRetires = new StrokeCollection();

        // Outil actif dans l'éditeur
        private string outilSelectionne = "crayon";

        public string OutilSelectionne
        {
            get { return outilSelectionne; }
            set
            {
                outilSelectionne = value;
                NotifyPropertyChanged();
            }
        }

        // Forme de la pointe du crayon
        private string pointeSelectionnee = "ronde";

        public string PointeSelectionnee
        {
            get { return pointeSelectionnee; }
            set
            {
                pointeSelectionnee = value;
                NotifyPropertyChanged();
            }
        }

        // Couleur des traits tracés par le crayon.
        private string couleurSelectionnee = "Black";

        public string CouleurSelectionnee
        {
            get { return couleurSelectionnee; }
            // Lorsqu'on sélectionne une couleur c'est généralement pour ensuite dessiner un trait.
            // C'est pourquoi lorsque la couleur est changée, l'outil est automatiquement changé pour le crayon.
            set
            {
                couleurSelectionnee = value;
                NotifyPropertyChanged();
            }
        }

        // Grosseur des traits tracés par le crayon.
        private int tailleTrait = 11;

        public int TailleTrait
        {
            get { return tailleTrait; }
            // Lorsqu'on sélectionne une taille de trait c'est généralement pour ensuite dessiner un trait.
            // C'est pourquoi lorsque la taille est changée, l'outil est automatiquement changé pour le crayon.
            set
            {
                tailleTrait = value;
                NotifyPropertyChanged();
            }
        }

        // On assigne une nouvelle forme de pointe passée en paramètre.
        public void ChoisirPointe(string pointe) => PointeSelectionnee = pointe;

        // L'outil actif devient celui passé en paramètre.
        public void ChoisirOutil(string outil) => OutilSelectionne = outil;

        // On vide la surface de dessin de tous ses traits.
    }
}