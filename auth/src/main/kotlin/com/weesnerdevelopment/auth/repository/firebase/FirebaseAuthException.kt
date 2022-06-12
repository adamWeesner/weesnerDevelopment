package com.weesnerdevelopment.auth.repository.firebase

import kotlinx.serialization.Serializable

@Serializable
internal data class FirebaseAuthException(
    val error: FirebaseError
)

@Serializable
internal data class FirebaseError(
    val code: Int,
    val message: String
)