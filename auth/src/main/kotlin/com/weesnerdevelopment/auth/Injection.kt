package com.weesnerdevelopment.auth

import Path
import auth.Cipher
import auth.JwtProvider
import auth.UserRouter
import auth.UsersService
import com.weesnerdevelopment.AppConfig
import history.HistoryService
import logging.LoggingService
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

val kodeinUser = Kodein.Module("user") {
    bind<JwtProvider>() with singleton {
        val appConfig = instance<AppConfig>()
        JwtProvider(appConfig.issuer, appConfig.audience, appConfig.expiresIn, Cipher(appConfig.secret))
    }

    bind<UsersService>() with singleton { UsersService(instance()) }
    // history
    bind<HistoryService>() with singleton { HistoryService() }
    // logging
    bind<LoggingService>() with singleton { LoggingService() }

    bind<UserRouter>() with singleton {
        UserRouter(Path.User.base, instance(), instance(), Path.User.account, Path.User.login, Path.User.signUp)
    }
}
