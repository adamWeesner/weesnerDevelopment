package com.weesnerdevelopment.auth.endpoints

import com.weesnerdevelopment.shared.Paths
import io.ktor.server.locations.*

@OptIn(KtorExperimentalLocationsAPI::class)
@Location(Paths.User.basePath)
data class UserEndpoint(val id: String? = null, val email: String? = null, val password: String? = null)