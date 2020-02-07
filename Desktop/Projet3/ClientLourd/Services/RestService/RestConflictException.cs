using System;

namespace ClientLourd.Services.RestService
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