package com.weesnerdevelopment.auth.user

import com.weesnerdevelopment.shared.Paths
import io.ktor.locations.*

@OptIn(KtorExperimentalLocationsAPI::class)
@Location(Paths.User.account)
object UserAccountEndpoint