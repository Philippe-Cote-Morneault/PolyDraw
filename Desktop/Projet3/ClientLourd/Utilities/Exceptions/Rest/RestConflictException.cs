using System;

namespace ClientLourd.Utilities.Exceptions.Rest
{
    public class RestConflictException : RestException
    {
        public RestConflictException()
        {
        }

        public RestConflictException(string message)
            : base(message)
        {
        }

        public RestConflictException(string message, Exception inner)
            : base(message, inner)
        {
        }
    }
}