package com.weesnerdevelopment.auth.repository.firebase.response

import kotlinx.serialization.Serializable

@Serializable
internal data class SignInResponse(
    val kind: String,
    /**
     * A Firebase Auth ID token for the newly created user.
     */
    val idToken: String,
    /**
     * The email for the authenticated user.
     */
    val email: String,
    /**
     * The display name for the user.
     */
    val displayName: String,
    /**
     * The uid of the newly created user.
     */
    val localId: String,
    /**
     * Whether the email is for an existing account.
     */
    val registered: Boolean
)