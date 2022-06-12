package com.weesnerdevelopment.auth.repository.firebase.request

import kotlinx.serialization.Serializable

@Serializable
internal data class SignInRequest(
    /**
     * The email for the user to create.
     */
    val email: String,
    /**
     * The password for the user to create.
     */
    val password: String,
    /**
     * Whether or not to return an ID and refresh token. Should always be true.
     */
    val returnSecureToken: Boolean = true
)