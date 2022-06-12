package com.weesnerdevelopment.auth

import auth.AuthValidator
import auth.AuthValidatorJwt
import auth.Cipher
import auth.JwtProvider
import com.weesnerdevelopment.auth.database.AuthDatabase
import com.weesnerdevelopment.auth.repository.UserRepository
import com.weesnerdevelopment.auth.repository.UserRepositoryImpl
import com.weesnerdevelopment.auth.router.UserRouter
import com.weesnerdevelopment.auth.router.UserRouterImpl
import com.weesnerdevelopment.businessRules.AppConfig
import com.weesnerdevelopment.businessRules.Environment
import com.weesnerdevelopment.businessRules.Server
import com.weesnerdevelopment.businessRules.auth.AuthConfig
import com.weesnerdevelopment.businessRules.auth.AuthProvider
import com.weesnerdevelopment.businessRules.auth.PrincipalUser
import io.ktor.server.application.*
import io.ktor.server.auth.*
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.ktor.di
import org.kodein.di.singleton

fun Application.initKodein() {
    di {
        bind<AppConfig>() with singleton { AppConfig(environment.config) }

        bind<JwtProvider>() with singleton {
            val appConfig = instance<AppConfig>()
            JwtProvider(appConfig.issuer, appConfig.audience, appConfig.expiresIn, Cipher(appConfig.secret))
        }
        bind<AuthValidator>() with singleton {
            val appConfig = instance<AppConfig>()

            when (Environment.valueOf(appConfig.appEnv)) {
                Environment.production, Environment.development -> throw IllegalArgumentException("Should be using the test implementation")
                Environment.testing -> AuthValidatorJwt
            }
        }
        bind<AuthDatabase>() with singleton {
            val appConfig = instance<AppConfig>()

            when (Environment.valueOf(appConfig.appEnv)) {
                Environment.production, Environment.development -> throw IllegalArgumentException("Should be using the test implementation")
                Environment.testing -> AuthDatabaseTesting
            }
        }

        bind<UserRepository>() with singleton { UserRepositoryImpl(instance()) }
        bind<UserRouter>() with singleton { UserRouterImpl(instance(), instance(), instance()) }

        bind<AuthConfig>() with singleton {
            object : AuthConfig(null) {
                override fun build(): AuthProvider = instance()
            }
        }
        bind<AuthProvider>() with singleton {
            object : AuthProvider, AuthenticationProvider(instance()) {
                override fun configure(authConfig: AuthenticationConfig) {
                    authConfig.register(this)
                }

                override suspend fun onAuthenticate(context: AuthenticationContext) {
                    context.principal(PrincipalUser("", "", ""))
                }
            }
        }
        bind<Server>() with singleton {
            val appConfig = instance<AppConfig>()

            when (Environment.valueOf(appConfig.appEnv)) {
                Environment.production, Environment.development -> throw IllegalArgumentException("Should be using the test implementation")
                Environment.testing -> AuthTestServer
            }
        }
    }
}
