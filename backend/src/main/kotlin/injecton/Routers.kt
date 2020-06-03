package com.weesnerdevelopment.injecton

import auth.UserRouter
import bills.BillsRouter
import categories.CategoriesRouter
import com.weesnerdevelopment.utils.Path.*
import federalIncomeTax.FederalIncomeTaxRouter
import income.IncomeRouter
import medicare.MedicareRouter
import occurrences.OccurrenceRouter
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import socialSecurity.SocialSecurityRouter
import taxWithholding.TaxWithholdingRouter

val routers = Kodein.Module("routers") {
    // user
    bind<UserRouter>() with singleton {
        UserRouter(User.base, instance(), instance(), instance(), User.account)
    }

    // taxFetcher
    bind<FederalIncomeTaxRouter>() with singleton {
        FederalIncomeTaxRouter(TaxFetcher.federalIncomeTax, instance(), instance(), instance())
    }
    bind<MedicareRouter>() with singleton {
        MedicareRouter(TaxFetcher.medicare, instance(), instance(), instance())
    }
    bind<SocialSecurityRouter>() with singleton {
        SocialSecurityRouter(TaxFetcher.socialSecurity, instance(), instance(), instance())
    }
    bind<TaxWithholdingRouter>() with singleton {
        TaxWithholdingRouter(TaxFetcher.taxWithholding, instance(), instance(), instance())
    }

    // billMan
    bind<BillsRouter>() with singleton {
        BillsRouter(BillMan.bills, instance(), instance(), instance(), instance(), instance())
    }
    bind<CategoriesRouter>() with singleton {
        CategoriesRouter(BillMan.categories, instance(), instance(), instance())
    }
    bind<IncomeRouter>() with singleton {
        IncomeRouter(BillMan.income, instance(), instance(), instance())
    }
    bind<OccurrenceRouter>() with singleton {
        OccurrenceRouter(BillMan.occurrences, instance(), instance(), instance(), instance(), instance())
    }
}
