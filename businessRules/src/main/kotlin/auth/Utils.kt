package auth

import org.apache.commons.codec.binary.Base64
import shared.auth.HashedUser

/**
 * Invalid User Exception to generate useful json formatted errors when there are user exceptions.
 */
data class InvalidUserException(
    val url: String,
    val statusCode: Int? = -1,
    val reasonCode: Int
)

/**
 * Reasons as to why there was a [InvalidUserException].
 */
enum class InvalidUserReason(val code: Int) {
    /**
     * Catch all for generic invalid user.
     */
    General(1000),

    /**
     * Expired user token.
     */
    Expired(1001),

    /**
     * Invalid formatted jwt, or missing jwt values.
     */
    InvalidJwt(1002),

    /**
     * No user found in the database matching the given credentials.
     */
    NoUserFound(1003),

    /**
     * Not valid hashed user data, ie: something is not valid [Base64] data.
     */
    InvalidUserInfo(1004),

    /**
     * Not a user that can access this.
     */
    WrongUser(1005)
}

/**
 * Verify data being passed for username and password are only made up of valid [Base64] string data.
 */
fun HashedUser.checkValidity(): InvalidUserReason? =
    if (!Base64.isBase64(username) || !Base64.isBase64(password)) InvalidUserReason.InvalidUserInfo
    else null
