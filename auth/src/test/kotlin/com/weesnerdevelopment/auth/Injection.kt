package com.weesnerdevelopment.auth

import auth.AuthValidator
import auth.AuthValidatorJwt
import auth.Cipher
import auth.JwtProvider
import com.weesnerdevelopment.auth.database.AuthDatabase
import com.weesnerdevelopment.auth.user.UserRepository
import com.weesnerdevelopment.auth.user.UserRepositoryImpl
import com.weesnerdevelopment.auth.user.UserRouter
import com.weesnerdevelopment.auth.user.UserRouterImpl
import com.weesnerdevelopment.businessRules.AppConfig
import com.weesnerdevelopment.businessRules.Environment
import com.weesnerdevelopment.businessRules.Server
import io.ktor.application.*
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import org.kodein.di.ktor.kodein

fun Application.initKodein() {
    kodein {
        bind<AppConfig>() with singleton { AppConfig(environment.config) }

        bind<JwtProvider>() with singleton {
            val appConfig = instance<AppConfig>()
            JwtProvider(appConfig.issuer, appConfig.audience, appConfig.expiresIn, Cipher(appConfig.secret))
        }
        bind<AuthValidator>() with singleton {
            val appConfig = instance<AppConfig>()

            when (Environment.valueOf(appConfig.appEnv)) {
                Environment.production, Environment.development ->
                    throw IllegalArgumentException("Should be using the test implementation")
                Environment.testing ->
                    AuthValidatorJwt
            }
        }
        bind<AuthDatabase>() with singleton {
            val appConfig = instance<AppConfig>()

            when (Environment.valueOf(appConfig.appEnv)) {
                Environment.production, Environment.development ->
                    throw IllegalArgumentException("Should be using the test implementation")
                Environment.testing ->
                    AuthDatabaseTesting
            }
        }

        bind<UserRepository>() with singleton { UserRepositoryImpl }
        bind<UserRouter>() with singleton { UserRouterImpl(instance(), instance(), instance()) }

        bind<Server>() with singleton {
            val appConfig = instance<AppConfig>()

            when (Environment.valueOf(appConfig.appEnv)) {
                Environment.production, Environment.development ->
                    throw IllegalArgumentException("Should be using the test implementation")
                Environment.testing ->
                    AuthTestServer
            }
        }
    }
}
