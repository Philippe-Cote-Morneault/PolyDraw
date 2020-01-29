using System;
using System.Net;
using ClientLourd.Utilities.Exceptions.Rest;
using MaterialDesignThemes.Wpf;
using RestSharp;

namespace ClientLourd.Services.Rest
{
    public class RestClient
    {
        private RestSharp.RestClient _client;
        private const string URL = "http://log3900.fsae.polymtl.ca:5000";

        public RestClient()
        {
            _client = new RestSharp.RestClient(URL);
        }

        public string Login(string username, string password)
        {
            RestRequest request = new RestRequest("auth");
            request.RequestFormat = DataFormat.Json;
            request.AddJsonBody(new {username = username});
            IRestResponse response= _client.Post(request);
            switch (response.StatusCode)
            {
                case HttpStatusCode.OK:
                    return response.Content;
                case HttpStatusCode.Conflict:
                    throw new RestConflictException(response.Content);
                case HttpStatusCode.BadRequest:
                    throw new RestBadRequestException(response.Content);
                default:
                    throw new RestException(response.Content);
                    
            }
        }
    }
}
