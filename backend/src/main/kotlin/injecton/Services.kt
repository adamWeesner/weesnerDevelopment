package com.weesnerdevelopment.injecton

import auth.UsersService
import billCategories.BillCategoriesService
import billSharedUsers.BillSharedUsersService
import bills.BillsService
import categories.CategoriesService
import colors.ColorsService
import federalIncomeTax.FederalIncomeTaxService
import history.HistoryService
import income.IncomeService
import medicare.MedicareLimitsService
import medicare.MedicareService
import occurrences.OccurrencesService
import occurrencesSharedUsers.OccurrenceSharedUsersService
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import payments.PaymentsService
import socialSecurity.SocialSecurityService
import taxWithholding.TaxWithholdingService

val services = Kodein.Module("services") {
    // user
    bind<UsersService>() with singleton { UsersService() }
    // history
    bind<HistoryService>() with singleton { HistoryService(instance()) }
    // taxFetcher
    bind<FederalIncomeTaxService>() with singleton { FederalIncomeTaxService() }
    bind<MedicareLimitsService>() with singleton { MedicareLimitsService() }
    bind<MedicareService>() with singleton { MedicareService(instance()) }
    bind<SocialSecurityService>() with singleton { SocialSecurityService() }
    bind<TaxWithholdingService>() with singleton { TaxWithholdingService() }
    // billMan
    bind<BillSharedUsersService>() with singleton { BillSharedUsersService(instance()) }
    bind<BillCategoriesService>() with singleton { BillCategoriesService(instance()) }
    bind<CategoriesService>() with singleton { CategoriesService(instance(), instance()) }
    bind<ColorsService>() with singleton { ColorsService(instance()) }
    bind<BillsService>() with singleton { BillsService(instance(), instance(), instance(), instance(), instance()) }
    bind<IncomeService>() with singleton { IncomeService(instance(), instance(), instance()) }
    bind<PaymentsService>() with singleton { PaymentsService(instance(), instance()) }
    bind<OccurrenceSharedUsersService>() with singleton { OccurrenceSharedUsersService(instance()) }
    bind<OccurrencesService>() with singleton { OccurrencesService(instance(), instance(), instance(), instance()) }
}
