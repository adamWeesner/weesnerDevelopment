package com.weesnerdevelopment.businessRules.auth

import io.ktor.server.application.*
import io.ktor.server.auth.*

/**
 * Configuration for the [AuthProvider] to set it up properly.
 */
abstract class AuthConfig(name: String?) : AuthenticationProvider.Config(name) {
    internal var token: (ApplicationCall) -> String? = { call -> call.request.parseAuthorizationToken() }

    internal var principal: ((uid: String) -> Principal?)? = null

    abstract fun build(): AuthProvider
}