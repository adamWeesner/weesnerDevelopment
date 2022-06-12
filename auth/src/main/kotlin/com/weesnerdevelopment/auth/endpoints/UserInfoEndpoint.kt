package com.weesnerdevelopment.auth.endpoints

import com.weesnerdevelopment.shared.Paths
import io.ktor.server.locations.*

@OptIn(KtorExperimentalLocationsAPI::class)
@Location(Paths.User.info)
data class UserInfoEndpoint(val id: String? = null)