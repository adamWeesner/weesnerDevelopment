package com.weesnerdevelopment.auth.repository.firebase.request

import kotlinx.serialization.Serializable

@Serializable
internal data class ChangeProfileRequest(
    /**
     * A Firebase Auth ID token for the user.
     */
    val idToken: String,
    /**
     * User's new display name.
     */
    val displayName: String? = null,
    /**
     * User's new photo url.
     */
    val photoUrl: String? = null,
    /**
     * List of attributes to delete, [ProfileAttributeToDelete.DISPLAY_NAME] and/or
     * [ProfileAttributeToDelete.PHOTO_URL]. This will nullify these values.
     */
    val deleteAttribute: List<String>? = null,
    /**
     * Whether or not to return an ID and refresh token.
     */
    val returnSecureToken: Boolean = true
)

internal enum class ProfileAttributeToDelete {
    DISPLAY_NAME, PHOTO_URL
}