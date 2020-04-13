using ClientLourd.Models.Bindable;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ClientLourd.ViewModels
{
    public class PublicProfileViewModel : ViewModelBase
    {
        User _user;

        public User User
        {
            get { return _user; }
            set
            {
                _user = value;
                NotifyPropertyChanged();
            }
        }

        //TODO: Get achievements

        public override void AfterLogin()
        {
            throw new NotImplementedException();
        }

        public override void AfterLogOut()
        {
            throw new NotImplementedException();
        }
    }
}