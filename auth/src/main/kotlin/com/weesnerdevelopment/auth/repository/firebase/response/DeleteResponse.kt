package com.weesnerdevelopment.auth.repository.firebase.response

import kotlinx.serialization.Serializable

@Serializable
internal data class DeleteResponse(
    val kind: String
)
