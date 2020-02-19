package auth

/**
 * Hashed user that has a hashed [username] and [password] that should be gotten from a JWT.
 */
data class HashedUser(
    val username: String,
    val password: String
)
