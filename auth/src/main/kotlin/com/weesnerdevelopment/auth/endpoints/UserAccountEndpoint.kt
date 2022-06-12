package com.weesnerdevelopment.auth.endpoints

import com.weesnerdevelopment.shared.Paths
import io.ktor.server.locations.*

@OptIn(KtorExperimentalLocationsAPI::class)
@Location(Paths.User.account)
object UserAccountEndpoint
