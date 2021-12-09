package com.weesnerdevelopment.auth.user

import Path
import io.ktor.locations.*

@OptIn(KtorExperimentalLocationsAPI::class)
@Location(Path.User.account)
object UserAccountEndpoint