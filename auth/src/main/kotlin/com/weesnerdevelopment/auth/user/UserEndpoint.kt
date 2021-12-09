package com.weesnerdevelopment.auth.user

import Path
import io.ktor.locations.*

@OptIn(KtorExperimentalLocationsAPI::class)
@Location(Path.User.basePath)
data class UserEndpoint(val id: String? = null, val username: String? = null, val password: String? = null)