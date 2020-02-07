﻿using System;

namespace ClientLourd.Models.Exceptions.Rest
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