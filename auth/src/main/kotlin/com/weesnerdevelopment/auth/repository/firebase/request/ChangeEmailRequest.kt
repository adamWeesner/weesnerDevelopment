package com.weesnerdevelopment.auth.repository.firebase.request

import kotlinx.serialization.Serializable

@Serializable
internal data class ChangeEmailRequest(
    /**
     * A Firebase Auth ID token for the user.
     */
    val idToken: String,
    /**
     * The user's new email.
     */
    val email: String,
    /**
     * Whether or not to return an ID and refresh token.
     */
    val returnSecureToken: Boolean = true
)