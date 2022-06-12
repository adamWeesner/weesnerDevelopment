package com.weesnerdevelopment.auth.repository.firebase.response

import kotlinx.serialization.Serializable

@Serializable
internal data class UpdateInfoResponse(
    val kind: String,
    /**
     * The uid of the current user.
     */
    val localId: String,
    /**
     * User's email address.
     */
    val email: String,
    /**
     * 	User's display name.
     */
    val displayName: String? = null,
    /**
     * User's photo url.
     */
    val photoUrl: String? = null,
    /**
     * New Firebase Auth ID token for user.
     */
    val idToken: String? = null,
    /**
     * List of all linked provider objects which contain "providerId" and "federatedId".
     */
    val providerUserInfo: List<ProviderInfo>,
    /**
     * Whether the account's email has been verified.
     */
    val emailVerified: Boolean
)