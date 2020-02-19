package auth

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
    NoUserFound(1003)
}
