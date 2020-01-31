using System;

namespace ClientLourd.Utilities.Exceptions.Rest
{
    public class RestException : Exception
    {
        public RestException()
        {
        }

        public RestException(string message)
            : base(message)
        {
        }

        public RestException(string message, Exception inner)
            : base(message, inner)
        {
        }
    }
}