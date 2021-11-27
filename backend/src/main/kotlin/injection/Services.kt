package com.weesnerdevelopment.injection

import auth.UsersService
import breathOfTheWild.cookingPotFood.CookingPotFoodsService
import breathOfTheWild.cookingPotFoodIngredients.CookingPotFoodIngredientsService
import breathOfTheWild.critter.CrittersService
import breathOfTheWild.effect.EffectsService
import breathOfTheWild.elixir.ElixirsService
import breathOfTheWild.elixirIngredients.ElixirIngredientsService
import breathOfTheWild.frozenFood.FrozenFoodsService
import breathOfTheWild.frozenFoodEffect.FrozenFoodEffectService
import breathOfTheWild.frozenFoodIngredients.FrozenFoodIngredientsService
import breathOfTheWild.image.ImagesService
import breathOfTheWild.ingredient.IngredientsService
import breathOfTheWild.ingredientBonusAddOns.IngredientBonusAddOnsService
import breathOfTheWild.ingredientDuration.IngredientDurationService
import breathOfTheWild.ingredientHearts.IngredientHeartsService
import breathOfTheWild.monsterPart.MonsterPartsService
import breathOfTheWild.otherFood.OtherFoodsService
import breathOfTheWild.otherFoodEffect.OtherFoodEffectService
import breathOfTheWild.otherFoodIngredients.OtherFoodIngredientsService
import breathOfTheWild.roastedFood.RoastedFoodsService
import breathOfTheWild.roastedFoodEffect.RoastedFoodEffectService
import breathOfTheWild.roastedFoodIngredients.RoastedFoodIngredientsService
import com.weesnerdevelopment.validator.ValidatorService
import com.weesnerdevelopment.validator.complex.ComplexValidatorService
import federalIncomeTax.FederalIncomeTaxService
import history.HistoryService
import logging.LoggingService
import medicare.MedicareLimitsService
import medicare.MedicareService
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import serialCabinet.electronic.ElectronicsService
import serialCabinet.itemCategories.ItemCategoriesService
import serialCabinet.manufacturer.ManufacturersService
import socialSecurity.SocialSecurityService
import taxWithholding.TaxWithholdingService
import serialCabinet.category.CategoriesService as SerialCategoriesService

val services = Kodein.Module("services") {
    bind<ValidatorService>() with singleton { ValidatorService() }
    bind<ComplexValidatorService>() with singleton { ComplexValidatorService(instance(), instance(), instance()) }

    bind<UsersService>() with singleton { UsersService(instance()) }
    bind<LoggingService>() with singleton { LoggingService() }
    // history
    bind<HistoryService>() with singleton { HistoryService() }
    // taxFetcher
    bind<FederalIncomeTaxService>() with singleton { FederalIncomeTaxService() }
    bind<MedicareLimitsService>() with singleton { MedicareLimitsService() }
    bind<MedicareService>() with singleton { MedicareService(instance()) }
    bind<SocialSecurityService>() with singleton { SocialSecurityService() }
    bind<TaxWithholdingService>() with singleton { TaxWithholdingService() }
    // breathOfTheWild
    bind<FrozenFoodEffectService>() with singleton { FrozenFoodEffectService(instance()) }
    bind<IngredientHeartsService>() with singleton { IngredientHeartsService(instance()) }
    bind<CookingPotFoodIngredientsService>() with singleton { CookingPotFoodIngredientsService() }
    bind<CookingPotFoodsService>() with singleton { CookingPotFoodsService(instance(), instance()) }
    bind<OtherFoodEffectService>() with singleton { OtherFoodEffectService(instance()) }
    bind<ElixirsService>() with singleton { ElixirsService(instance(), instance()) }
    bind<FrozenFoodsService>() with singleton { FrozenFoodsService(instance(), instance(), instance()) }
    bind<CrittersService>() with singleton { CrittersService() }
    bind<FrozenFoodIngredientsService>() with singleton { FrozenFoodIngredientsService() }
    bind<OtherFoodsService>() with singleton { OtherFoodsService(instance(), instance(), instance()) }
    bind<EffectsService>() with singleton { EffectsService(instance()) }
    bind<MonsterPartsService>() with singleton { MonsterPartsService() }
    bind<RoastedFoodIngredientsService>() with singleton { RoastedFoodIngredientsService() }
    bind<OtherFoodIngredientsService>() with singleton { OtherFoodIngredientsService() }
    bind<ImagesService>() with singleton { ImagesService() }
    bind<IngredientBonusAddOnsService>() with singleton { IngredientBonusAddOnsService(instance()) }
    bind<IngredientDurationService>() with singleton { IngredientDurationService() }
    bind<ElixirIngredientsService>() with singleton { ElixirIngredientsService() }
    bind<RoastedFoodsService>() with singleton { RoastedFoodsService(instance(), instance(), instance()) }
    bind<IngredientsService>() with singleton { IngredientsService(instance(), instance(), instance(), instance()) }
    bind<RoastedFoodEffectService>() with singleton { RoastedFoodEffectService(instance()) }

    // serialCabinet
    bind<ManufacturersService>() with singleton { ManufacturersService() }
    bind<SerialCategoriesService>() with singleton { SerialCategoriesService() }
    bind<ElectronicsService>() with singleton { ElectronicsService(instance(), instance(), instance(), instance()) }
    bind<ItemCategoriesService>() with singleton { ItemCategoriesService(instance()) }
}