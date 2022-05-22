package com.weesnerdevelopment.auth

import auth.AuthValidator
import auth.AuthValidatorFake
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

        bind<AuthValidator>() with singleton {
            val appConfig = instance<AppConfig>()

            when (Environment.valueOf(appConfig.appEnv)) {
                Environment.production, Environment.development ->
                    throw IllegalArgumentException("Should be using the test implementation")
                Environment.testing ->
                    AuthValidatorFake("e0f97212-4431-448b-bd97-f70235328cd1")
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
