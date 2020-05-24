package generics

import auth.InvalidUserException
import io.ktor.http.HttpStatusCode

class InvalidAttributeException(value: String) : IllegalArgumentException("$value is required but missing or invalid")

sealed class Response(val status: HttpStatusCode, val message: Any)
class Ok(message: Any?) : Response(HttpStatusCode.OK, message ?: "")
class Created(message: Any?) : Response(HttpStatusCode.Created, message ?: "")
class BadRequest(message: String) : Response(HttpStatusCode.BadRequest, message)
class NotFound(message: String) : Response(HttpStatusCode.NotFound, message)
class Conflict(message: String) : Response(HttpStatusCode.Conflict, message)
class InternalError(message: String) : Response(HttpStatusCode.InternalServerError, message)
class Unauthorized(reason: InvalidUserException) : Response(HttpStatusCode.Unauthorized, reason)
