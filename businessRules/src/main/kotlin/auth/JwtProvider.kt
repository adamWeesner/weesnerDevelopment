package auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import shared.auth.HashedUser
import java.util.*

class JwtProvider(
    private val issuer: String,
    private val audience: String,
    private val validFor: Long,
    private val cipher: auth.Cipher
) {
    /**
     * Builds a [JWTVerifier] with the given [issuer].
     */
    val verifier: JWTVerifier = JWT
        .require(cipher.algorithm)
        .withIssuer(issuer)
        .build()

    /**
     * Decodes the given [token].
     */
    fun decodeJWT(token: String): DecodedJWT = JWT.require(cipher.algorithm).build().verify(token)

    /**
     * Create a JWT token for the given [hashedUser].
     */
    fun createJWT(hashedUser: HashedUser): String? = JWT.create()
        .withIssuedAt(Date())
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withAudience(audience)
        .withClaim("attr-username", hashedUser.username)
        .withClaim("attr-password", hashedUser.password)
        .withExpiresAt(Date(System.currentTimeMillis() + validFor)).sign(cipher.algorithm)
}

class Cipher(
    secret: String
) {
    val algorithm = Algorithm.HMAC256(secret)

    fun encrypt(data: String?): ByteArray = algorithm.sign(data?.toByteArray())
}