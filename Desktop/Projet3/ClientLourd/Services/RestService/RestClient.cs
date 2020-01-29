using System;
using RestSharp;

namespace ClientLourd.Services.Rest
{
    class RestClient
    {
        private RestSharp.RestClient _client;
        private const string URL = "http://log3900.fsae.polymtl.ca:5000";

        public RestClient()
        {
            _client = new RestSharp.RestClient(URL);
        }

        public void Login(string username, string password)
        {
            RestRequest request = new RestRequest("hello");
            IRestResponse response= _client.Get(request);

            Console.WriteLine(response.Content);
        }
    }
}
