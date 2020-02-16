namespace ClientLourd.Models.Bindable
{
    public class User : ModelBase
    {
        public User()
        {
            Username = "";
            FirstName = "";
            LastName = "";
            Email = "";
            ID = "";
            PictureID = "";
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
            PictureID = user.PictureID;
            CreatedAt = user.CreatedAt;
            UpdatedAt = user.UpdatedAt;
        }

        public User(string username, string id)
        {
            Username = username;
            ID = id;
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
         
         private string _pictureID;
         public string PictureID
         {
             get { return _pictureID; }
             set
             {
                 if (value != _pictureID)
                 {
                     _pictureID = value;
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
                     user1.ID == user2.ID);
             
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
                     user1.ID != user2.ID);
         }
    }
}