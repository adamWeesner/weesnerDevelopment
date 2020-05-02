package com.weesnerdevelopment.injecton

import auth.UserRouter
import bills.BillsRouter
import categories.CategoriesRouter
import com.weesnerdevelopment.utils.Path.*
import federalIncomeTax.FederalIncomeTaxRouter
import income.IncomeRouter
import medicare.MedicareRouter
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import socialSecurity.SocialSecurityRouter
import taxWithholding.TaxWithholdingRouter

val routers = Kodein.Module("routers") {
    // user
    bind<UserRouter>() with singleton { UserRouter(User.base, instance(), instance(), User.account) }
    // taxFetcher
    bind<FederalIncomeTaxRouter>() with singleton { FederalIncomeTaxRouter(TaxFetcher.federalIncomeTax, instance()) }
    bind<MedicareRouter>() with singleton { MedicareRouter(TaxFetcher.medicare, instance()) }
    bind<SocialSecurityRouter>() with singleton { SocialSecurityRouter(TaxFetcher.socialSecurity, instance()) }
    bind<TaxWithholdingRouter>() with singleton { TaxWithholdingRouter(TaxFetcher.taxWithholding, instance()) }
    // billMan
    bind<BillsRouter>() with singleton { BillsRouter(BillMan.bills, instance()) }
    bind<CategoriesRouter>() with singleton { CategoriesRouter(BillMan.categories, instance()) }
    bind<IncomeRouter>() with singleton { IncomeRouter(BillMan.income, instance()) }
}
