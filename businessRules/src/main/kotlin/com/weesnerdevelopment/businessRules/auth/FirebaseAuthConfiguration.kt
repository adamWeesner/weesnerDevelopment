package com.weesnerdevelopment.businessRules.auth

/**
 * Configuration for the [FirebaseAuthProvider] to set it up properly, if this is being used.
 */
class FirebaseAuthConfiguration(name: String?) : AuthConfig(name) {
    override fun build() = FirebaseAuthProvider(this)
}
