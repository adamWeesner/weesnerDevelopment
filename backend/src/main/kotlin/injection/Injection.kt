package com.weesnerdevelopment.injection

import com.weesnerdevelopment.businessRules.AppConfig
import io.ktor.application.*
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton
import org.kodein.di.ktor.kodein

fun Application.kodeinSetup() {
    kodein {
        bind<AppConfig>() with singleton { AppConfig(environment.config) }

        import(services)
        import(routers)
    }
}
