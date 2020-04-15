package auth

import com.squareup.moshi.JsonClass

fun HashedUser.asToken(jwtProvider: JwtProvider) = jwtProvider.createJWT(this)

@JsonClass(generateAdapter = true)
data class TokenResponse(val token: String?)
