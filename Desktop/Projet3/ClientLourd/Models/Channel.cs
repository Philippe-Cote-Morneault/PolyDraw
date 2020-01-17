using System.Collections.ObjectModel;

namespace ClientLourd.Models
{
    public class Channel: ModelBase
    {
        public Channel() {}

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
                    _messages = value;
                    NotifyPropertyChanged();
                }
                
            }
        }
        private ObservableCollection<Message> _messages;

    }
}