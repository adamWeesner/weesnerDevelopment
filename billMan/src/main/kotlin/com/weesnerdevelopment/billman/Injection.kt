package com.weesnerdevelopment.billman

import Path
import com.weesnerdevelopment.AppConfig
import com.weesnerdevelopment.auth.kodeinUser
import com.weesnerdevelopment.billman.billCategories.BillCategoriesService
import com.weesnerdevelopment.billman.billSharedUsers.BillSharedUsersService
import com.weesnerdevelopment.billman.bills.*
import com.weesnerdevelopment.billman.categories.CategoriesRouter
import com.weesnerdevelopment.billman.categories.CategoriesService
import com.weesnerdevelopment.billman.colors.ColorsService
import com.weesnerdevelopment.billman.income.IncomeRouter
import com.weesnerdevelopment.billman.income.IncomeService
import com.weesnerdevelopment.billman.incomeOccurrences.IncomeOccurrenceRouter
import com.weesnerdevelopment.billman.incomeOccurrences.IncomeOccurrencesService
import com.weesnerdevelopment.billman.occurrences.BillOccurrenceRouter
import com.weesnerdevelopment.billman.occurrences.BillOccurrencesService
import com.weesnerdevelopment.billman.occurrencesSharedUsers.OccurrenceSharedUsersService
import com.weesnerdevelopment.billman.payments.PaymentsService
import io.ktor.application.*
import logging.LoggingRouter
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import org.kodein.di.ktor.kodein

fun Application.initKodein() {
    kodein {
        bind<AppConfig>() with singleton { AppConfig(environment.config) }

        import(kodeinUser)
        bind<BillsRouter>() with singleton { BillsRouterImpl(instance()) }
        bind<BillsRepository>() with singleton { BillsRepositoryImpl }

        bind<BillSharedUsersService>() with singleton { BillSharedUsersService(instance()) }
        bind<BillCategoriesService>() with singleton { BillCategoriesService(instance()) }
        bind<CategoriesService>() with singleton { CategoriesService(instance(), instance()) }
        bind<ColorsService>() with singleton { ColorsService() }
        bind<BillsService>() with singleton { BillsService(instance(), instance(), instance(), instance(), instance()) }
        bind<IncomeService>() with singleton { IncomeService(instance(), instance(), instance()) }
        bind<PaymentsService>() with singleton { PaymentsService(instance(), instance()) }
        bind<OccurrenceSharedUsersService>() with singleton { OccurrenceSharedUsersService(instance()) }
        bind<BillOccurrencesService>() with singleton {
            BillOccurrencesService(instance(), instance(), instance(), instance())
        }
        bind<IncomeOccurrencesService>() with singleton { IncomeOccurrencesService(instance(), instance()) }

        bind<CategoriesRouter>() with singleton { CategoriesRouter(Path.BillMan.categories, instance()) }
        bind<IncomeRouter>() with singleton { IncomeRouter(Path.BillMan.income, instance()) }
        bind<BillOccurrenceRouter>() with singleton { BillOccurrenceRouter(Path.BillMan.occurrences, instance()) }
        bind<IncomeOccurrenceRouter>() with singleton {
            IncomeOccurrenceRouter(Path.BillMan.incomeOccurrences, instance())
        }
        bind<LoggingRouter>() with singleton { LoggingRouter(Path.BillMan.logging, instance()) }
    }
}
