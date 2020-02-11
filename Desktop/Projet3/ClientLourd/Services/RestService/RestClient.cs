using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Net;
using System.Threading.Tasks;
using System.Windows.Documents;
using ClientLourd.Models.Bindable;
using ClientLourd.Services.RestService.Exceptions;
using ClientLourd.Utilities.Constants;
using Newtonsoft.Json;
using RestSharp;
using RestSharp.Serialization.Json;

namespace ClientLourd.Services.RestService
{
    public class RestClient
    {
        private RestSharp.RestClient _client;
        private string _sessionToken;
        public RestClient()
        {
            // For local server usage
            /*_client = new RestSharp.RestClient("http://127.0.0.1:3000")
            {
                Timeout = 10000,
            };*/

            _client = new RestSharp.RestClient($"http://{Networks.HOST_NAME}:{Networks.REST_PORT}")
            {
                Timeout = 10000,
            };
        }
        
        /// <summary>
        /// Try to login using the username and password specified
        /// </summary>
        /// <param name="username"></param>
        /// <param name="password"></param>
        /// <returns></returns>
        /// <exception cref="RestConflictException"></exception>
        /// <exception cref="RestBadRequestException"></exception>
        /// <exception cref="RestException"></exception>
        public async Task<Dictionary<string, object>> Login(string username, string password)
        {
            RestRequest request = new RestRequest("auth", Method.POST);
            request.RequestFormat = DataFormat.Json;
            request.AddJsonBody(new {username = username});
            var response = await Execute(request);
            var deseralizer = new JsonDeserializer();
            switch (response.StatusCode)
            {
                case HttpStatusCode.OK:
                    dynamic data = deseralizer.Deserialize<dynamic>(response);
                    _sessionToken = data["SessionToken"];
                    return data;
                case HttpStatusCode.Conflict:
                    throw new RestConflictException(deseralizer.Deserialize<dynamic>(response)["Error"]);
                case HttpStatusCode.BadRequest:
                    throw new RestBadRequestException(deseralizer.Deserialize<dynamic>(response)["Error"]);
                default:
                    throw new RestException(response.ErrorMessage);
            }
        }
        
        public async Task<List<Channel>> GetChannels()
        {
            RestRequest request = new RestRequest("chat/channels", Method.GET);
            request.AddParameter("SessionToken", _sessionToken, ParameterType.HttpHeader);
            var response = await Execute(request);
            var deseralizer = new JsonDeserializer();
            switch (response.StatusCode)
            {
                case HttpStatusCode.OK:
                    var channels = JsonConvert.DeserializeObject<List<Channel>>(response.Content);
                    channels.ForEach(c => c.Messages = new ObservableCollection<Message>());
                    return channels;
                case HttpStatusCode.Unauthorized:
                    throw new RestUnauthorizedException(deseralizer.Deserialize<dynamic>(response)["Error"]);
                default:
                    throw new RestException(response.ErrorMessage);
            }
        }
        
        public async Task<Channel> GetChannel(string channelId)
        {
            RestRequest request = new RestRequest("chat/channels", Method.GET);
            //TODO mettre dans le bon format
            request.AddParameter("channelID", channelId);
            var response = await Execute(request);
            var deseralizer = new JsonDeserializer();
            switch (response.StatusCode)
            {
                case HttpStatusCode.OK:
                    return JsonConvert.DeserializeObject<Channel>(response.Content);
                case HttpStatusCode.Unauthorized:
                    throw new RestUnauthorizedException(deseralizer.Deserialize<dynamic>(response)["Error"]);
                case HttpStatusCode.NotFound:
                    throw new RestNotFoundException(deseralizer.Deserialize<dynamic>(response)["Error"]);
                default:
                    throw new RestException(response.ErrorMessage);
            }
        }

        public async Task<PrivateProfileInfo> GetUserInfo(string userID)
        {
            RestRequest request = new RestRequest($"users/{userID}", Method.GET);
            //request.AddParameter("userid", userID);
            request.AddParameter("SessionToken", _sessionToken, ParameterType.HttpHeader);
            var response = await Execute(request);
            var deseralizer = new JsonDeserializer();
            switch (response.StatusCode)
            {
                case HttpStatusCode.OK:
                    return JsonConvert.DeserializeObject<PrivateProfileInfo>(response.Content);
                case HttpStatusCode.BadRequest:
                    throw new RestBadRequestException(deseralizer.Deserialize<dynamic>(response)["Error"]);
                case HttpStatusCode.Unauthorized:
                    throw new RestUnauthorizedException(deseralizer.Deserialize<dynamic>(response)["Error"]);
                case HttpStatusCode.NotFound:
                    throw new RestNotFoundException(deseralizer.Deserialize<dynamic>(response)["Error"]);
                default:
                    throw new RestException(response.ErrorMessage);
            }
        }


        private Task<IRestResponse> Execute(RestRequest request)
        {
            Task<IRestResponse> task = new Task<IRestResponse>(() =>
            {
                OnStartWaiting(this);
                var response = _client.Execute(request);
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