using System;
using System.ComponentModel;
using System.Net;
using ClientLourd.Utilities.Exceptions.Rest;
using MaterialDesignThemes.Wpf;
using RestSharp;
using RestSharp.Extensions;
using RestSharp.Serialization.Json;

namespace ClientLourd.Services.Rest
{
    public class RestClient
    {
        private RestSharp.RestClient _client;
        //private const string URL = "http://log3900.fsae.polymtl.ca:5000";
        private const string URL = "http://127.0.0.1:3000";

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
            var deseralizer = new JsonDeserializer();
            switch (response.StatusCode)
            {
                case HttpStatusCode.OK:
                    var tokens = deseralizer.Deserialize<dynamic>(response);
                    return tokens["SessionToken"];
                case HttpStatusCode.Conflict:
                    throw new RestConflictException(deseralizer.Deserialize<dynamic>(response)["Error"]);
                case HttpStatusCode.BadRequest:
                    throw new RestBadRequestException(deseralizer.Deserialize<dynamic>(response)["Error"]);
                default:
                    throw new RestException();
                    
            }
        }
    }
}
