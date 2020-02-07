using System;
using System.Net;
using System.Threading.Tasks;
using ClientLourd.Models.Constants;
using ClientLourd.Models.Exceptions.Rest;
using RestSharp;
using RestSharp.Serialization.Json;

namespace ClientLourd.Services.RestService
{
    public class RestClient
    {
        private RestSharp.RestClient _client;
        public RestClient()
        {
            // For local server usage
            //_client = new RestSharp.RestClient("http://127.0.0.1:3000")

            _client = new RestSharp.RestClient($"http://{Networks.HOST_NAME}:{Networks.REST_PORT}")
            {
                Timeout = 10000,
            };
        }

        public async Task<string> Login(string username, string password)
        {
            RestRequest request = new RestRequest("auth");
            request.RequestFormat = DataFormat.Json;
            request.AddJsonBody(new {username = username});
            var response = await Execute(request);
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
throw new RestException(response.ErrorMessage);
            }
        }

        private Task<IRestResponse> Execute(RestRequest request)
        {
            Task<IRestResponse> task = new Task<IRestResponse>(() =>
            {
                OnStartWaiting(this);
                var response = _client.Post(request);
                OnStopWaiting(this);
                return response;
            });
            task.Start();
            return task;
        }

        public delegate void RestEventHandler(object source, EventArgs args);

        public event RestEventHandler StartWaiting;
        public event RestEventHandler StopWaiting;

        protected virtual void OnStartWaiting(object source)
        {
            StartWaiting?.Invoke(source, EventArgs.Empty);
        }

        protected virtual void OnStopWaiting(object source)
        {
            StopWaiting?.Invoke(source, EventArgs.Empty);
        }
    }
}