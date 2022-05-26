package auth

import com.auth0.jwt.exceptions.JWTVerificationException
import io.ktor.application.*
import io.ktor.util.pipeline.*
import loggedUserData
import java.util.*

interface AuthValidator {
    fun getUuid(call: PipelineContext<Unit, ApplicationCall>): String
}

object AuthValidatorJwt : AuthValidator {
    override fun getUuid(call: PipelineContext<Unit, ApplicationCall>): String =
        call.call.loggedUserData()?.getData()?.let {
            runCatching {
                UUID.fromString(it.uuid)?.toString()
            }.getOrNull()
        } ?: throw JWTVerificationException("Jwt data could not be parsed")
}

class AuthValidatorFake(
    val uuid: String
) : AuthValidator {
    override fun getUuid(call: PipelineContext<Unit, ApplicationCall>): String = uuid
}