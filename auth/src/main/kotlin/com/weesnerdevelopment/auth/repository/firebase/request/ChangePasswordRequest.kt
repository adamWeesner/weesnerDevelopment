package com.weesnerdevelopment.auth.repository.firebase.request

import kotlinx.serialization.Serializable

@Serializable
internal data class ChangePasswordRequest(
    /**
     * A Firebase Auth ID token for the user.
     */
    val idToken: String,
    /**
     * User's new password.
     */
    val password: String,
    /**
     * Whether or not to return an ID and refresh token.
     */
    val returnSecureToken: Boolean = true
)