package com.weesnerdevelopment.routes

import bills.BillsRouter
import categories.CategoriesRouter
import generics.route
import income.IncomeRouter
import incomeOccurrences.IncomeOccurrenceRouter
import io.ktor.auth.authenticate
import io.ktor.routing.Routing
import occurrences.BillOccurrenceRouter
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein

fun Routing.billManRoutes() {
    val billsRouter by kodein().instance<BillsRouter>()
    val categoriesRouter by kodein().instance<CategoriesRouter>()
    val incomeRouter by kodein().instance<IncomeRouter>()
    val occurrencesRouter by kodein().instance<BillOccurrenceRouter>()
    val incomeOccurrencesRouter by kodein().instance<IncomeOccurrenceRouter>()

    authenticate {
        billsRouter.apply {
            authenticate {
                setupRoutes()
            }
        }
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
        route(incomeOccurrencesRouter)
    }
}
