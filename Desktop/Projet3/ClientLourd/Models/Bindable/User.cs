using System;
using System.Data;
using System.Text.RegularExpressions;
using System.Windows.Media.Imaging;

namespace ClientLourd.Models.Bindable
{
    public class User : ModelBase
    {
        
        
        private bool _isCPU;
        public User()
        {
            IsCPU = false;
            Username = "";
            FirstName = "";
            LastName = "";
            Email = "";
            ID = "";
            Avatar = null;
            CreatedAt = "";
            UpdatedAt = "";
        }

        public User(User user)
        {
            Username = user.Username;
            FirstName = user.FirstName;
            LastName = user._lastName;
            Email = user.Email;
            ID = user.ID;
            Avatar = user.Avatar;
            CreatedAt = user.CreatedAt;
            UpdatedAt = user.UpdatedAt;
            IsCPU = user.IsCPU;
        }

        public User(string username, string id, bool isCPU)
        {
            Username = username;
            ID = id;
            IsCPU = isCPU;
        }

        public void Update(User user)
        {
            Username = user.Username;
            FirstName = user.FirstName;
            LastName = user._lastName;
            Email = user.Email;
            ID = user.ID;
            Avatar = user.Avatar;
            CreatedAt = user.CreatedAt;
            UpdatedAt = user.UpdatedAt;
        }

        public bool IsCPU
        {
            get => _isCPU;
            set
            {
                if (value != _isCPU)
                {
                    _isCPU = value;
                    if (_isCPU)
                    {
                        _avatar = new BitmapImage(new Uri($"/ClientLourd;component/Resources/robot.png", UriKind.Relative));
                    }
                    NotifyPropertyChanged();
                }
            }
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

         private BitmapImage _avatar;
         public BitmapImage Avatar
         {
             get
             {
                 return _avatar;
             }
             set
             {
                 if (value != _avatar)
                 {
                     _avatar = value;
                     try
                     {
                         _pictureID = int.Parse(Regex.Match(Avatar.UriSource.ToString(), @"\d+").Value);
                     }
                     catch
                     {
                         _pictureID = 0;
                     }
                     NotifyPropertyChanged();
                     NotifyPropertyChanged(nameof(PictureID));
                 }
             }
         }

         private int _pictureID;
         public int PictureID
         {
             get { return _pictureID; }
             set
             {
                 if (_pictureID != value)
                 {
                     _pictureID = value;
                     _avatar = new BitmapImage(new Uri($"/ClientLourd;component/Resources/Avatar/{_pictureID}.jpg", UriKind.Relative));
                     NotifyPropertyChanged();
                     NotifyPropertyChanged(nameof(Avatar));
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
 
 
         public static bool operator ==(User user1, User user2)
         {
             if (ReferenceEquals(user1, user2))
             {
                 return true;
             }
 
             if (ReferenceEquals(user1, null))
             {
                 return false;
             }
             if (ReferenceEquals(user2, null))
             {
                 return false;
             }
 
             return (user1.Username == user2.Username && user1.Email == user2.Email &&
                     user1.FirstName == user2.FirstName && user1.LastName == user2.LastName &&
                     user1.ID == user2.ID && user1.PictureID == user2.PictureID);
             
         }
 
         public static bool operator !=(User user1, User user2)
         {
             if (ReferenceEquals(user1, user2))
             {
                 return false;
             }
 
             if (ReferenceEquals(user1, null))
             {
                 return true;
             }
             if (ReferenceEquals(user2, null))
             {
                 return true;
             }
 
             return (user1.Username != user2.Username || user1.Email != user2.Email ||
                     user1.FirstName != user2.FirstName || user1.LastName != user2.LastName ||
                     user1.ID != user2.ID || user1.PictureID != user2.PictureID);
         }
    }
}