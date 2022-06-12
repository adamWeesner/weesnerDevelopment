package com.weesnerdevelopment.businessRules.auth

import io.ktor.server.auth.*
import io.ktor.server.request.*

/**
 * Authorization provider used for getting [Principal] information about the person attempting to
 * make a request.
 */
interface AuthProvider {
    /**
     * Use this in the [install] block for [Authentication] to set up the auth provider, linking it
     * to the application.
     */
    fun configure(authConfig: AuthenticationConfig)
}

fun ApplicationRequest.parseAuthorizationToken(): String? = authorization()?.let {
    it.split(" ")[1]
}
