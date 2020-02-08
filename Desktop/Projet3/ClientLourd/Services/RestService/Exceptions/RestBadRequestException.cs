using System;

namespace ClientLourd.Services.RestService
{
    public class RestBadRequestException : RestException
    {
        public RestBadRequestException()
        {
        }

        public RestBadRequestException(string message)
            : base(message)
        {
        }

        public RestBadRequestException(string message, Exception inner)
            : base(message, inner)
        {
        }
    }
}