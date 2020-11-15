package com.weesnerdevelopment.injection

import auth.UsersService
import billCategories.BillCategoriesService
import billSharedUsers.BillSharedUsersService
import bills.BillsService
import breathOfTheWild.cookingPotFood.CookingPotFoodsService
import breathOfTheWild.cookingPotIngredients.CookingPotIngredientsService
import breathOfTheWild.critter.CrittersService
import breathOfTheWild.elixirIngredients.ElixirIngredientsService
import breathOfTheWild.elixirs.ElixirsService
import breathOfTheWild.images.ImagesService
import breathOfTheWild.ingredients.IngredientsService
import breathOfTheWild.ingredientsBonusAddOns.BonusAddOnsService
import breathOfTheWild.ingredientsDuration.IngredientDurationsService
import breathOfTheWild.ingredientsHearts.IngredientHeartsService
import categories.CategoriesService
import colors.ColorsService
import com.weesnerdevelopment.validator.ValidatorService
import com.weesnerdevelopment.validator.complex.ComplexValidatorService
import federalIncomeTax.FederalIncomeTaxService
import history.HistoryService
import income.IncomeService
import incomeOccurrences.IncomeOccurrencesService
import logging.LoggingService
import medicare.MedicareLimitsService
import medicare.MedicareService
import occurrences.BillOccurrencesService
import occurrencesSharedUsers.OccurrenceSharedUsersService
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import payments.PaymentsService
import socialSecurity.SocialSecurityService
import taxWithholding.TaxWithholdingService

val services = Kodein.Module("services") {
    bind<ValidatorService>() with singleton { ValidatorService() }
    bind<ComplexValidatorService>() with singleton { ComplexValidatorService(instance(), instance(), instance()) }
    // user
    bind<UsersService>() with singleton { UsersService(instance()) }
    // history
    bind<HistoryService>() with singleton { HistoryService() }
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
    bind<ColorsService>() with singleton { ColorsService() }
    bind<BillsService>() with singleton { BillsService(instance(), instance(), instance(), instance(), instance()) }
    bind<IncomeService>() with singleton { IncomeService(instance(), instance(), instance()) }
    bind<PaymentsService>() with singleton { PaymentsService(instance(), instance()) }
    bind<OccurrenceSharedUsersService>() with singleton { OccurrenceSharedUsersService(instance()) }
    bind<BillOccurrencesService>() with singleton {
        BillOccurrencesService(instance(), instance(), instance(), instance())
    }
    bind<IncomeOccurrencesService>() with singleton { IncomeOccurrencesService(instance(), instance()) }
    bind<LoggingService>() with singleton { LoggingService() }
    // breathOfTheWild
    bind<ImagesService>() with singleton { ImagesService() }
    bind<CrittersService>() with singleton { CrittersService() }
    bind<CookingPotIngredientsService>() with singleton { CookingPotIngredientsService() }
    bind<CookingPotFoodsService>() with singleton { CookingPotFoodsService(instance(), instance()) }
    bind<IngredientHeartsService>() with singleton { IngredientHeartsService(instance()) }
    bind<BonusAddOnsService>() with singleton { BonusAddOnsService(instance()) }
    bind<IngredientDurationsService>() with singleton { IngredientDurationsService() }
    bind<IngredientsService>() with singleton { IngredientsService(instance(), instance(), instance(), instance()) }
    bind<ElixirIngredientsService>() with singleton { ElixirIngredientsService() }
    bind<ElixirsService>() with singleton { ElixirsService(instance(), instance()) }
}
