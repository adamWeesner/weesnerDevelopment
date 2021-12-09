package auth

import com.weesnerdevelopment.shared.auth.HashedUser
import com.weesnerdevelopment.shared.auth.User

fun HashedUser.asToken(jwtProvider: JwtProvider) = jwtProvider.createJWT(this)
fun User.asToken(jwtProvider: JwtProvider) = jwtProvider.createJWT(this)
