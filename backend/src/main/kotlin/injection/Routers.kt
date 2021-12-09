package com.weesnerdevelopment.injection

import Path.*
import auth.Cipher
import auth.JwtProvider
import auth.UserRouter
import breathOfTheWild.cookingPotFood.CookingPotFoodsRouter
import breathOfTheWild.critter.CrittersRouter
import breathOfTheWild.effect.EffectsRouter
import breathOfTheWild.elixir.ElixirsRouter
import breathOfTheWild.frozenFood.FrozenFoodsRouter
import breathOfTheWild.image.ImagesRouter
import breathOfTheWild.ingredient.IngredientsRouter
import breathOfTheWild.monsterPart.MonsterPartsRouter
import breathOfTheWild.otherFood.OtherFoodsRouter
import breathOfTheWild.roastedFood.RoastedFoodsRouter
import com.weesnerdevelopment.businessRules.AppConfig
import com.weesnerdevelopment.validator.ValidatorRouter
import com.weesnerdevelopment.validator.complex.ComplexValidatorRouter
import federalIncomeTax.FederalIncomeTaxRouter
import medicare.MedicareRouter
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import serialCabinet.electronic.ElectronicsRouter
import serialCabinet.manufacturer.ManufacturersRouter
import socialSecurity.SocialSecurityRouter
import taxWithholding.TaxWithholdingRouter
import serialCabinet.category.CategoriesRouter as SerialCategoriesRouter

val routers = Kodein.Module("routers") {
    bind<JwtProvider>() with singleton {
        val appConfig = instance<AppConfig>()
        JwtProvider(appConfig.issuer, appConfig.audience, appConfig.expiresIn, Cipher(appConfig.secret))
    }

    bind<ValidatorRouter>() with singleton { ValidatorRouter(Server.validation, instance()) }
    bind<ComplexValidatorRouter>() with singleton { ComplexValidatorRouter(Server.complexValidation, instance()) }
    // user
    bind<UserRouter>() with singleton {
        UserRouter(User.basePath, instance(), instance(), User.account, User.login, User.signUp)
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

    // breathOfTheWild
    bind<CookingPotFoodsRouter>() with singleton { CookingPotFoodsRouter(BreathOfTheWild.cookingPotFoods, instance()) }
    bind<ElixirsRouter>() with singleton { ElixirsRouter(BreathOfTheWild.elixirs, instance()) }
    bind<FrozenFoodsRouter>() with singleton { FrozenFoodsRouter(BreathOfTheWild.frozenFoods, instance()) }
    bind<CrittersRouter>() with singleton { CrittersRouter(BreathOfTheWild.critters, instance()) }
    bind<OtherFoodsRouter>() with singleton { OtherFoodsRouter(BreathOfTheWild.otherFoods, instance()) }
    bind<EffectsRouter>() with singleton { EffectsRouter(BreathOfTheWild.effects, instance()) }
    bind<MonsterPartsRouter>() with singleton { MonsterPartsRouter(BreathOfTheWild.monsterParts, instance()) }
    bind<RoastedFoodsRouter>() with singleton { RoastedFoodsRouter(BreathOfTheWild.roastedFoods, instance()) }
    bind<IngredientsRouter>() with singleton { IngredientsRouter(BreathOfTheWild.ingredients, instance()) }
    bind<ImagesRouter>() with singleton { ImagesRouter(BreathOfTheWild.images, instance()) }

    // serialCabinet
    bind<ManufacturersRouter>() with singleton { ManufacturersRouter(SerialCabinet.manufacturers, instance()) }
    bind<SerialCategoriesRouter>() with singleton { SerialCategoriesRouter(SerialCabinet.categories, instance()) }
    bind<ElectronicsRouter>() with singleton { ElectronicsRouter(SerialCabinet.electronics, instance()) }
}