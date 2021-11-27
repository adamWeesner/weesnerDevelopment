package com.weesnerdevelopment.billman

import com.weesnerdevelopment.billman.bills.BillsRouter
import com.weesnerdevelopment.billman.categories.CategoriesRouter
import com.weesnerdevelopment.billman.income.IncomeRouter
import com.weesnerdevelopment.billman.incomeOccurrences.IncomeOccurrenceRouter
import com.weesnerdevelopment.billman.occurrences.BillOccurrenceRouter
import io.ktor.auth.*
import io.ktor.routing.*
import logging.LoggingRouter
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein

fun Routing.routes() {
    val billsRouter by kodein().instance<BillsRouter>()
    val categoriesRouter by kodein().instance<CategoriesRouter>()
    val incomeRouter by kodein().instance<IncomeRouter>()
    val occurrencesRouter by kodein().instance<BillOccurrenceRouter>()
    val incomeOccurrencesRouter by kodein().instance<IncomeOccurrenceRouter>()
    val loggingRouter by kodein().instance<LoggingRouter>()

    billsRouter.setup(this)

    authenticate {
        categoriesRouter.apply {
            authenticate {
                setupRoutes()
            }
        }
        incomeRouter.apply {
            authenticate {
                setupRoutes()
            }
        }
        occurrencesRouter.apply {
            authenticate {
                setupRoutes()
            }
        }
        incomeOccurrencesRouter.apply {
            authenticate {
                setupRoutes()
            }
        }
        loggingRouter.apply {
            authenticate {
                setupRoutes()
            }
        }
    }
}