package com.weesnerdevelopment.billman

import auth.AuthValidator
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
import com.weesnerdevelopment.billman.income.IncomeRepository
import com.weesnerdevelopment.billman.income.IncomeRepositoryImpl
import com.weesnerdevelopment.billman.income.IncomeRouter
import com.weesnerdevelopment.billman.income.IncomeRouterImpl
import com.weesnerdevelopment.billman.income.occurrence.IncomeOccurrenceRepository
import com.weesnerdevelopment.billman.income.occurrence.IncomeOccurrenceRepositoryImpl
import com.weesnerdevelopment.billman.income.occurrence.IncomeOccurrenceRouter
import com.weesnerdevelopment.billman.income.occurrence.IncomeOccurrenceRouterImpl
import com.weesnerdevelopment.businessRules.AppConfig
import io.ktor.application.*
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import org.kodein.di.ktor.kodein

fun Application.initKodein(
    authValidator: AuthValidator
) {
    kodein {
        bind<AppConfig>() with singleton { AppConfig(environment.config) }

        bind<JwtProvider>() with singleton {
            val appConfig = instance<AppConfig>()
            JwtProvider(appConfig.issuer, appConfig.audience, appConfig.expiresIn, Cipher(appConfig.secret))
        }

        bind<AuthValidator>() with singleton { authValidator }

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
    }
}
