package com.weesnerdevelopment.injection

import auth.Cipher
import auth.JwtProvider
import com.weesnerdevelopment.AppConfig
import io.ktor.application.*
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import org.kodein.di.ktor.kodein

fun Application.kodeinSetup() {
    kodein {
        bind<AppConfig>() with singleton { AppConfig(environment.config) }
        bind<JwtProvider>() with singleton {
            val appConfig = instance<AppConfig>()
            JwtProvider(appConfig.issuer, appConfig.audience, appConfig.expiresIn, Cipher(appConfig.secret))
        }

        import(services)
        import(routers)
    }
}
