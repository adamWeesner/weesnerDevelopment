package com.weesnerdevelopment.auth

import auth.AuthValidator
import auth.AuthValidatorFirebase
import auth.Cipher
import auth.JwtProvider
import com.weesnerdevelopment.auth.database.AuthDatabase
import com.weesnerdevelopment.auth.database.AuthDatabaseProd
import com.weesnerdevelopment.auth.repository.UserRepository
import com.weesnerdevelopment.auth.repository.firebase.UserRepositoryFirebase
import com.weesnerdevelopment.auth.router.UserRouter
import com.weesnerdevelopment.auth.router.UserRouterImpl
import com.weesnerdevelopment.auth.server.AuthDevServer
import com.weesnerdevelopment.auth.server.AuthProdServer
import com.weesnerdevelopment.businessRules.AppConfig
import com.weesnerdevelopment.businessRules.Environment
import com.weesnerdevelopment.businessRules.Server
import com.weesnerdevelopment.businessRules.auth.AuthConfig
import com.weesnerdevelopment.businessRules.auth.AuthProvider
import com.weesnerdevelopment.businessRules.auth.FirebaseAuthConfiguration
import com.weesnerdevelopment.businessRules.auth.FirebaseAuthProvider
import io.ktor.server.application.*
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
                Environment.development -> AuthValidatorFirebase
                Environment.production -> AuthValidatorFirebase
                Environment.testing -> throw IllegalArgumentException("Should not be using the test implementation")
            }
        }
        bind<AuthDatabase>() with singleton {
            val appConfig = instance<AppConfig>()

            when (Environment.valueOf(appConfig.appEnv)) {
                Environment.development -> AuthDatabaseProd
                Environment.production -> AuthDatabaseProd
                Environment.testing -> throw IllegalArgumentException("Should not be using the test implementation")
            }
        }
        bind<UserRepository>() with singleton { UserRepositoryFirebase }
        bind<UserRouter>() with singleton { UserRouterImpl(instance(), instance(), instance()) }
        bind<Server>() with singleton {
            val appConfig = instance<AppConfig>()

            when (Environment.valueOf(appConfig.appEnv)) {
                Environment.development -> AuthDevServer
                Environment.production -> AuthProdServer
                Environment.testing -> throw IllegalArgumentException("Should not be using the test implementation")
            }
        }
        bind<AuthConfig>() with singleton { FirebaseAuthConfiguration(null) }
        bind<AuthProvider>() with singleton { FirebaseAuthProvider(instance(), "../") }
    }
}
