package com.weesnerdevelopment.routes

import bills.BillsRouter
import categories.CategoriesRouter
import generics.route
import income.IncomeRouter
import io.ktor.auth.authenticate
import io.ktor.routing.Routing
import occurrences.OccurrenceRouter
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein

fun Routing.billManRoutes() {
    val billsRouter by kodein().instance<BillsRouter>()
    val categoriesRouter by kodein().instance<CategoriesRouter>()
    val incomeRouter by kodein().instance<IncomeRouter>()
    val occurrencesRouter by kodein().instance<OccurrenceRouter>()

    authenticate {
        route(billsRouter)
        route(categoriesRouter)
        route(incomeRouter)
        route(occurrencesRouter) { router ->
            (router as OccurrenceRouter).apply {
                pay()
            }
        }
    }
}
