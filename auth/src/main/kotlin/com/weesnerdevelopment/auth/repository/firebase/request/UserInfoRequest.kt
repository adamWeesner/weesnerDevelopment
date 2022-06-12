package com.weesnerdevelopment.auth.repository.firebase.request

import kotlinx.serialization.Serializable

@Serializable
internal data class UserInfoRequest(
    /**
     * The Firebase ID token of the account.
     */
    val idToken: String
)