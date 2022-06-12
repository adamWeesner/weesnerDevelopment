package com.weesnerdevelopment.businessRules.auth

import io.ktor.server.auth.*

/**
 * Basic User class for auth and uid validation for things like having access to certain things
 */
data class PrincipalUser(
    val uid: String,
    val name: String?,
    val email: String?
) : Principal