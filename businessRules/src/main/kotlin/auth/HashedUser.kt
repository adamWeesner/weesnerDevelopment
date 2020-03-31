package auth

import com.squareup.moshi.JsonClass

/**
 * Hashed user that has a hashed [username] and [password] that should be gotten from a JWT.
 */
data class HashedUser(
    val username: String,
    val password: String
) {
    fun asToken(jwtProvider: JwtProvider) = jwtProvider.createJWT(this)
}

@JsonClass(generateAdapter = true)
data class TokenResponse(val token: String?)
