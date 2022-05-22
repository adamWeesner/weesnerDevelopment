package com.weesnerdevelopment.billman

import auth.AuthValidator
import auth.AuthValidatorFake
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
import com.weesnerdevelopment.billman.income.IncomeRepository
import com.weesnerdevelopment.billman.income.IncomeRepositoryImpl
import com.weesnerdevelopment.billman.income.IncomeRouter
import com.weesnerdevelopment.billman.income.IncomeRouterImpl
import com.weesnerdevelopment.billman.income.occurrence.IncomeOccurrenceRepository
import com.weesnerdevelopment.billman.income.occurrence.IncomeOccurrenceRepositoryImpl
import com.weesnerdevelopment.billman.income.occurrence.IncomeOccurrenceRouter
import com.weesnerdevelopment.billman.income.occurrence.IncomeOccurrenceRouterImpl
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
        bind<BillManDatabase>() with singleton {
            val appConfig = instance<AppConfig>()

            when (Environment.valueOf(appConfig.appEnv)) {
                Environment.production, Environment.development ->
                    throw IllegalArgumentException("Should be using the test implementation")
                Environment.testing ->
                    BillManDatabaseTesting
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
                Environment.production, Environment.development ->
                    throw IllegalArgumentException("Should be using the test implementation")
                Environment.testing ->
                    BillManTestServer
            }
        }
    }
}
