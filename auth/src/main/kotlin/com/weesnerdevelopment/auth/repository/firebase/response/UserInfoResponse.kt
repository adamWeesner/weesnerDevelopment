package com.weesnerdevelopment.auth.repository.firebase.response

import kotlinx.serialization.Serializable

@Serializable
internal data class UserInfoResponse(
    val kind: String,
    /**
     * The account associated with the given Firebase ID token. Check below for more details.
     */
    val users: List<FirebaseUser>
)

@Serializable
internal data class FirebaseUser(
    /**
     * The uid of the current user.
     */
    val localId: String,
    /**
     * The email of the account.
     */
    val email: String,
    /**
     * The display name for the account, if there is one.
     */
    val displayName: String? = null,
    /**
     * Whether or not the account's email has been verified.
     */
    val emailVerified: Boolean,
    /**
     * List of all linked provider objects which contain "providerId" and "federatedId".
     */
    val providerUserInfo: List<ProviderInfo>,
    /**
     * The photo Url for the account, if there is one.
     */
    val photoUrl: String? = null,
    /**
     * The timestamp, in milliseconds, that the account last logged in at.
     */
    val lastLoginAt: String,
    /**
     * The timestamp, in milliseconds, that the account was created at.
     */
    val createdAt: String
)

@Serializable
internal data class ProviderInfo(
    val providerId: String,
    /**
     * The display name for the account, if there is one.
     */
    val displayName: String? = null,
    /**
     * The photo Url for the account, if there is one.
     */
    val photoUrl: String? = null,
    /**
     * The email of the account.
     */
    val email: String
)