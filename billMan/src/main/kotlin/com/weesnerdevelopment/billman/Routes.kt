package com.weesnerdevelopment.billman

import com.weesnerdevelopment.billman.bill.BillsRouter
import com.weesnerdevelopment.billman.bill.occurrence.BillOccurrenceRouter
import com.weesnerdevelopment.billman.category.CategoriesRouter
import com.weesnerdevelopment.billman.income.IncomeRouter
import com.weesnerdevelopment.billman.income.occurrence.IncomeOccurrenceRouter
import io.ktor.routing.*
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein

fun Routing.routes() {
    val billsRouter by kodein().instance<BillsRouter>()
    val billOccurrencesRouter by kodein().instance<BillOccurrenceRouter>()
    val categoriesRouter by kodein().instance<CategoriesRouter>()
    val incomesRouter by kodein().instance<IncomeRouter>()
    val incomeOccurrencesRouter by kodein().instance<IncomeOccurrenceRouter>()

    billsRouter.setup(this)
    billOccurrencesRouter.setup(this)
    categoriesRouter.setup(this)
    incomesRouter.setup(this)
    incomeOccurrencesRouter.setup(this)
}