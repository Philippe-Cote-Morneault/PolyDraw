using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using RestSharp;
using MessagePack.Formatters;
namespace ClientLourd.Services.Network
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
