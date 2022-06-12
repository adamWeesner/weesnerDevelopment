package com.weesnerdevelopment.businessRules.auth

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.ErrorCode
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.weesnerdevelopment.businessRules.Log
import com.weesnerdevelopment.businessRules.respondUnauthorized
import com.weesnerdevelopment.shared.auth.InvalidUserReason
import io.ktor.server.application.*
import io.ktor.server.auth.*
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

data class FirebaseAuthProvider(
    private val configuration: AuthConfig,
    private val accountFileBasePath: String = ""
) : AuthProvider, AuthenticationProvider(configuration) {
    private val accountFile = File("${accountFileBasePath}firebase-admin.json")
    private val serviceAccount: InputStream = FileInputStream(accountFile)

    private val options: FirebaseOptions = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .build()

    private val token: (ApplicationCall) -> String? get() = configuration.token
    private val principal: ((uid: String) -> Principal?)? get() = configuration.principal

    init {
        runCatching {
            FirebaseApp.initializeApp(options)
        }
    }

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        try {
            val token = token(context.call) ?: throw FirebaseAuthException(
                FirebaseException(ErrorCode.UNAUTHENTICATED, "No token could be found", null)
            )

            val user = FirebaseAuth.getInstance().verifyIdToken(token)

            configuration.principal =
                { PrincipalUser(uid = user.uid, name = user.name, email = user.email) }

            principal?.let {
                it(user.uid)?.let { principal ->
                    context.principal(principal)
                }
            }
        } catch (cause: Throwable) {
            val message = if (cause is FirebaseAuthException) {
                "Authentication failed: ${cause.message ?: cause.javaClass.simpleName}"
            } else {
                cause.message ?: cause.javaClass.simpleName
            }
            Log.trace(message)
            context.challenge.complete()
            context.call.respondUnauthorized(InvalidUserReason.InvalidJwt)
        }
    }

    override fun configure(authConfig: AuthenticationConfig) {
        authConfig.register(this)
    }
}