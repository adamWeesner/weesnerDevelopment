package auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm

private val algorithm = Algorithm.HMAC256("secret")

fun makeJwtVerifier(issuer: String, audience: String): JWTVerifier = JWT
    .require(algorithm)
    .withAudience(audience)
    .withIssuer(issuer)
    .build()

data class InvalidUserException(
    val url: String,
    val statusCode: Int? = -1,
    val reasonCode: Int
)

enum class InvalidUserReason(val code: Int) {
    General(1000),
    Expired(1001),
    InvalidJwt(1002)
}
