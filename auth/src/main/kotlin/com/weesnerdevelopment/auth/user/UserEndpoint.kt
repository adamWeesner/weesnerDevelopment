package com.weesnerdevelopment.auth.user

import com.weesnerdevelopment.shared.Paths
import io.ktor.locations.*

@OptIn(KtorExperimentalLocationsAPI::class)
@Location(Paths.User.basePath)
data class UserEndpoint(val id: String? = null, val username: String? = null, val password: String? = null)