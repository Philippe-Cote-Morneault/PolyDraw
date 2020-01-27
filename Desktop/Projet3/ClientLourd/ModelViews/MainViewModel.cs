using ClientLourd.Services.Rest;
using ClientLourd.Services.SocketService;

namespace ClientLourd.ModelViews
{
    class MainViewModel: ViewModelBase
    {

        string _username;
        public RestClient _restClient;
        public SocketClient _socketClient;

        public MainViewModel()
        {
            _username = "";
            _restClient = new RestClient();
            _socketClient = new SocketClient("1234");
        }

        public string Username
        {
            get
            {
                return _username;
            }

            set
            {
                if (value != _username)
                {
                    _username = value;
                    NotifyPropertyChanged();
                }
            }
        }



    }

    
}
