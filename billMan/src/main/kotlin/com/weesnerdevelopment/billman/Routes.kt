package com.weesnerdevelopment.billman

import com.weesnerdevelopment.billman.bill.BillsRouter
import com.weesnerdevelopment.billman.category.CategoriesRouter
import com.weesnerdevelopment.billman.income.IncomeRouter
import io.ktor.routing.*
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein

fun Routing.routes() {
    val billsRouter by kodein().instance<BillsRouter>()
    val categoriesRouter by kodein().instance<CategoriesRouter>()
    val incomesRouter by kodein().instance<IncomeRouter>()

    billsRouter.setup(this)
    categoriesRouter.setup(this)
    incomesRouter.setup(this)
}