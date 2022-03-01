package com.weesnerdevelopment.auth

import auth.Cipher
import auth.JwtProvider
import com.weesnerdevelopment.auth.user.UserRepository
import com.weesnerdevelopment.auth.user.UserRepositoryImpl
import com.weesnerdevelopment.auth.user.UserRouter
import com.weesnerdevelopment.auth.user.UserRouterImpl
import com.weesnerdevelopment.businessRules.AppConfig
import io.ktor.application.*
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import org.kodein.di.ktor.kodein

fun Application.initKodein() {
    kodein {
        bind<AppConfig>() with singleton { AppConfig(environment.config) }

        import(kodeinUser)
    }
}

val kodeinUser = Kodein.Module("user") {
    bind<JwtProvider>() with singleton {
        val appConfig = instance<AppConfig>()
        JwtProvider(appConfig.issuer, appConfig.audience, appConfig.expiresIn, Cipher(appConfig.secret))
    }

    bind<UserRepository>() with singleton { UserRepositoryImpl }
    bind<UserRouter>() with singleton { UserRouterImpl(instance(), instance()) }
}
