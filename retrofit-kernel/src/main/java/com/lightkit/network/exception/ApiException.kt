package com.lightkit.network.exception

sealed class ApiException(message: String) : Exception(message) {

    class Unauthorized(message: String) : ApiException(message)

    class ServerError(message: String) : ApiException(message)

    class NetworkError(message: String) : ApiException(message)

    class OtherError(val code: Int, message: String) : ApiException(message)
}
