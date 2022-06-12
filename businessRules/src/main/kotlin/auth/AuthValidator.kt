package auth

import com.auth0.jwt.exceptions.JWTVerificationException
import com.weesnerdevelopment.businessRules.auth.parseAuthorizationToken
import com.weesnerdevelopment.businessRules.loggedUserData
import io.ktor.server.application.*
import io.ktor.util.pipeline.*
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

object AuthValidatorFirebase : AuthValidator {
    override fun getUuid(call: PipelineContext<Unit, ApplicationCall>): String =
        call.call.request.parseAuthorizationToken() ?: ""
}

class AuthValidatorFake(
    val uuid: String
) : AuthValidator {
    override fun getUuid(call: PipelineContext<Unit, ApplicationCall>): String = uuid
}