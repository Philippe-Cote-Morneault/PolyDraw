using System.Windows.Controls;

namespace ClientLourd.Models.NonBindable
{
    public class DialogRequest
    {
        public DialogRequest(UserControl dialog, bool closeOnClickAway)
        {
            
        }
        
        public UserControl Dialog { get; set; }
        public bool CloseOnClickAway { get; set; }
    }
}