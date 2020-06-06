package auth

import shared.auth.HashedUser

fun HashedUser.asToken(jwtProvider: JwtProvider) = jwtProvider.createJWT(this)
