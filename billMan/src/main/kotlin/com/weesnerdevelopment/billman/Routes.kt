package com.weesnerdevelopment.billman

import com.weesnerdevelopment.billman.bill.BillsRouter
import com.weesnerdevelopment.billman.bill.occurrence.BillOccurrenceRouter
import com.weesnerdevelopment.billman.category.CategoriesRouter
import com.weesnerdevelopment.billman.income.IncomeRouter
import com.weesnerdevelopment.billman.income.occurrence.IncomeOccurrenceRouter
import io.ktor.server.routing.*
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI

fun Route.routes() {
    val billsRouter by closestDI().instance<BillsRouter>()
    val billOccurrencesRouter by closestDI().instance<BillOccurrenceRouter>()
    val categoriesRouter by closestDI().instance<CategoriesRouter>()
    val incomesRouter by closestDI().instance<IncomeRouter>()
    val incomeOccurrencesRouter by closestDI().instance<IncomeOccurrenceRouter>()

    billsRouter.setup(this)
    billOccurrencesRouter.setup(this)
    categoriesRouter.setup(this)
    incomesRouter.setup(this)
    incomeOccurrencesRouter.setup(this)
}