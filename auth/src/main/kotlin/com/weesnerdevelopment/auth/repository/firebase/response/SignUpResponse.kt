package com.weesnerdevelopment.auth.repository.firebase.response

import kotlinx.serialization.Serializable

@Serializable
internal data class SignUpResponse(
    val kind: String,
    /**
     * A Firebase Auth ID token for the newly created user.
     */
    val idToken: String,
    /**
     * The email for the newly created user.
     */
    val email: String,
    /**
     * A Firebase Auth refresh token for the newly created user.
     */
    val refreshToken: String,
    /**
     * The number of seconds in which the ID token expires.
     */
    val expiresIn: String,
    /**
     * The uid of the newly created user.
     */
    val localId: String
)