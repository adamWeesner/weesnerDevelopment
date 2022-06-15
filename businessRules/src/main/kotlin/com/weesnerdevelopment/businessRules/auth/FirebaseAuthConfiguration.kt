package com.weesnerdevelopment.businessRules.auth

/**
 * Configuration for the [FirebaseAuthProvider] to set it up properly, if this is being used.
 */
object FirebaseAuthConfiguration : AuthConfig(null) {
    override fun build() = FirebaseAuthProvider(this)
}
