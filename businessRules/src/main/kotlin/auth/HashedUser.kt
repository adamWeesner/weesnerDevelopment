package auth

import com.weesnerdevelopment.shared.auth.HashedUser

fun HashedUser.asToken(jwtProvider: JwtProvider) = jwtProvider.createJWT(this)
