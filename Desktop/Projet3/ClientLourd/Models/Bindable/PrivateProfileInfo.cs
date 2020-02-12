using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ClientLourd.Models.Bindable
{
    public class PrivateProfileInfo : ModelBase
    {
        public PrivateProfileInfo()
        {

        }


        public PrivateProfileInfo(PrivateProfileInfo clone)
        {
            ID = clone.ID;
            FirstName = clone.FirstName;
            LastName = clone.LastName;
            Email = clone.Email;
            Username = clone.Username;
        }

        private string _id;
        public string ID
        {
            get { return _id; }
            set
            {
                if (value != _id)
                {
                    _id = value;
                    NotifyPropertyChanged();
                }
            }
        }

        private string _firstName;
        public string FirstName
        {
            get { return _firstName; }
            set
            {
                if (value != _firstName)
                {
                    _firstName = value;
                    NotifyPropertyChanged();
                }
            }
        }

        private string _lastName;
        public string LastName
        {
            get { return _lastName; }
            set
            {
                if (value != _lastName)
                {
                    _lastName = value;
                    NotifyPropertyChanged();
                }
            }
        }


        private string _username;
        public string Username
        {
            get { return _username; }
            set
            {
                if (value != _username)
                {
                    _username = value;
                    NotifyPropertyChanged();
                }
            }
        }

        private string _email;
        public string Email
        {
            get { return _email; }
            set
            {
                if (value != _email)
                {
                    _email = value;
                    NotifyPropertyChanged();
                }
            }
        }


        private string _createdAt;
        public string CreatedAt
        {
            get { return _createdAt; }
            set
            {
                if (value != _createdAt)
                {
                    _createdAt = value;
                    NotifyPropertyChanged();
                }
            }
        }

        private string _updatedAt;
        public string UpdatedAt
        {
            get { return _updatedAt; }
            set
            {
                if (value != _updatedAt)
                {
                    _updatedAt = value;
                    NotifyPropertyChanged();
                }
            }
        }


        

    }

    



}
