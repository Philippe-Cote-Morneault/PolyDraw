using System.Collections.ObjectModel;
using System.Linq;

namespace ClientLourd.Models
{
    public class Channel : ModelBase
    {
        public Channel()
        {
        }

        public string Name
        {
            get { return _name; }
            set
            {
                if (value != _name)
                {
                    _name = value;
                    NotifyPropertyChanged();
                }
            }
        }

        private string _name;

        public ObservableCollection<Message> Messages
        {
            get { return _messages; }
            set
            {
                if (value != _messages)
                {
                    _messages = new ObservableCollection<Message>(value.OrderBy(m => m.Date).ToList());
                    NotifyPropertyChanged();
                }
            }
        }

        private ObservableCollection<Message> _messages;
    }
}