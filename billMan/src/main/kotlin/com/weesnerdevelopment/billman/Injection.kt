package com.weesnerdevelopment.billman

import auth.AuthValidator
import auth.AuthValidatorFirebase
import auth.Cipher
import auth.JwtProvider
import com.weesnerdevelopment.billman.bill.BillsRepository
import com.weesnerdevelopment.billman.bill.BillsRepositoryImpl
import com.weesnerdevelopment.billman.bill.BillsRouter
import com.weesnerdevelopment.billman.bill.BillsRouterImpl
import com.weesnerdevelopment.billman.bill.occurrence.BillOccurrenceRepository
import com.weesnerdevelopment.billman.bill.occurrence.BillOccurrenceRepositoryImpl
import com.weesnerdevelopment.billman.bill.occurrence.BillOccurrenceRouter
import com.weesnerdevelopment.billman.bill.occurrence.BillOccurrenceRouterImpl
import com.weesnerdevelopment.billman.category.CategoriesRepository
import com.weesnerdevelopment.billman.category.CategoriesRepositoryImpl
import com.weesnerdevelopment.billman.category.CategoriesRouter
import com.weesnerdevelopment.billman.category.CategoriesRouterImpl
import com.weesnerdevelopment.billman.database.BillManDatabase
import com.weesnerdevelopment.billman.database.BillManDatabaseProd
import com.weesnerdevelopment.billman.income.IncomeRepository
import com.weesnerdevelopment.billman.income.IncomeRepositoryImpl
import com.weesnerdevelopment.billman.income.IncomeRouter
import com.weesnerdevelopment.billman.income.IncomeRouterImpl
import com.weesnerdevelopment.billman.income.occurrence.IncomeOccurrenceRepository
import com.weesnerdevelopment.billman.income.occurrence.IncomeOccurrenceRepositoryImpl
import com.weesnerdevelopment.billman.income.occurrence.IncomeOccurrenceRouter
import com.weesnerdevelopment.billman.income.occurrence.IncomeOccurrenceRouterImpl
import com.weesnerdevelopment.billman.server.BillManDevServer
import com.weesnerdevelopment.billman.server.BillManProdServer
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
        bind<BillManDatabase>() with singleton {
            val appConfig = instance<AppConfig>()

            when (Environment.valueOf(appConfig.appEnv)) {
                Environment.development -> BillManDatabaseProd
                Environment.production -> BillManDatabaseProd
                Environment.testing -> throw IllegalArgumentException("Should not be using the test implementation")
            }
        }

        bind<BillsRepository>() with singleton { BillsRepositoryImpl }
        bind<BillsRouter>() with singleton { BillsRouterImpl(instance(), instance(), instance()) }

        bind<BillOccurrenceRepository>() with singleton { BillOccurrenceRepositoryImpl }
        bind<BillOccurrenceRouter>() with singleton { BillOccurrenceRouterImpl(instance(), instance()) }

        bind<CategoriesRepository>() with singleton { CategoriesRepositoryImpl }
        bind<CategoriesRouter>() with singleton { CategoriesRouterImpl(instance(), instance()) }

        bind<IncomeRepository>() with singleton { IncomeRepositoryImpl }
        bind<IncomeRouter>() with singleton { IncomeRouterImpl(instance(), instance()) }

        bind<IncomeOccurrenceRepository>() with singleton { IncomeOccurrenceRepositoryImpl }
        bind<IncomeOccurrenceRouter>() with singleton { IncomeOccurrenceRouterImpl(instance(), instance()) }

        bind<Server>() with singleton {
            val appConfig = instance<AppConfig>()

            when (Environment.valueOf(appConfig.appEnv)) {
                Environment.development -> BillManDevServer
                Environment.production -> BillManProdServer
                Environment.testing -> throw IllegalArgumentException("Should not be using the test implementation")
            }
        }
        bind<AuthConfig>() with singleton { FirebaseAuthConfiguration(null) }
        bind<AuthProvider>() with singleton { FirebaseAuthProvider(instance()) }
    }
}
