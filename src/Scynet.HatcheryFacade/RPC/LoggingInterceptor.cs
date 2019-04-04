using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Grpc.Core;
using Grpc.Core.Interceptors;
using Microsoft.Extensions.Logging;


namespace Scynet.HatcheryFacade.RPC
{
    public class LoggingInterceptor : Interceptor
    {
        private readonly ILogger<LoggingInterceptor> _logger;
        public LoggingInterceptor(ILogger<LoggingInterceptor> logger) => _logger = logger;

        public override AsyncServerStreamingCall<TResponse> AsyncServerStreamingCall<TRequest, TResponse>(TRequest request,
            ClientInterceptorContext<TRequest, TResponse> context, AsyncServerStreamingCallContinuation<TRequest, TResponse> continuation)
        {
            _logger.LogDebug($"Called from: {context.Host}, Method: {context.Method}");
            try
            {
                return continuation(request, context);
            }
            catch (Exception err)
            {
                _logger.LogError(err.ToString());
                throw;
            }
        }


        public override async Task<TResponse> UnaryServerHandler<TRequest, TResponse>(TRequest request, ServerCallContext context, UnaryServerMethod<TRequest, TResponse> continuation)
        {
            _logger.LogDebug($"Called from: {context.Host}, Method: {context.Method}, Peer: {context.Peer}, Status: {context.Deadline}");
            try
            {
                return await continuation(request, context);
            }
            catch (Exception err)
            {
                _logger.LogError(err.ToString());
                throw;
            }
        }
    }
}
