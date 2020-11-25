package com.weesnerdevelopment.seed

import breathOfTheWild.cookingPotFood.CookingPotFoodsService
import breathOfTheWild.cookingPotFood.CookingPotFoodsTable
import breathOfTheWild.cookingPotFoodIngredients.CookingPotFoodIngredientsTable
import breathOfTheWild.critter.CrittersService
import breathOfTheWild.critter.CrittersTable
import breathOfTheWild.effect.EffectsService
import breathOfTheWild.effect.EffectsTable
import breathOfTheWild.elixir.ElixirsService
import breathOfTheWild.elixir.ElixirsTable
import breathOfTheWild.elixirIngredients.ElixirIngredientsTable
import breathOfTheWild.frozenFood.FrozenFoodsService
import breathOfTheWild.frozenFood.FrozenFoodsTable
import breathOfTheWild.frozenFoodEffect.FrozenFoodEffectTable
import breathOfTheWild.frozenFoodIngredients.FrozenFoodIngredientsTable
import breathOfTheWild.image.ImagesTable
import breathOfTheWild.ingredient.IngredientsService
import breathOfTheWild.ingredient.IngredientsTable
import breathOfTheWild.ingredientBonusAddOns.IngredientBonusAddOnsTable
import breathOfTheWild.ingredientDuration.IngredientDurationTable
import breathOfTheWild.ingredientHearts.IngredientHeartsTable
import breathOfTheWild.monsterPart.MonsterPartsService
import breathOfTheWild.monsterPart.MonsterPartsTable
import breathOfTheWild.otherFood.OtherFoodsService
import breathOfTheWild.otherFood.OtherFoodsTable
import breathOfTheWild.otherFoodEffect.OtherFoodEffectTable
import breathOfTheWild.otherFoodIngredients.OtherFoodIngredientsTable
import breathOfTheWild.roastedFood.RoastedFoodsService
import breathOfTheWild.roastedFood.RoastedFoodsTable
import breathOfTheWild.roastedFoodEffect.RoastedFoodEffectTable
import breathOfTheWild.roastedFoodIngredients.RoastedFoodIngredientsTable
import io.ktor.application.*
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.SchemaUtils.drop
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein
import shared.fromJson
import shared.zelda.responses.*
import java.io.File

suspend fun Application.breathOfTheWildSeed() {
    val ingredientsService by kodein().instance<IngredientsService>()
    val effectsService by kodein().instance<EffectsService>()
    val cookingPotFoodsService by kodein().instance<CookingPotFoodsService>()
    val elixirsService by kodein().instance<ElixirsService>()
    val frozenFoodsService by kodein().instance<FrozenFoodsService>()
    val monsterPartsService by kodein().instance<MonsterPartsService>()
    val otherFoodsService by kodein().instance<OtherFoodsService>()
    val roastedFoodsService by kodein().instance<RoastedFoodsService>()

    suspend fun seedCritters() {
        val crittersService by kodein().instance<CrittersService>()
        val items = parseFileAsClass<CrittersResponse>("critters")?.items?.toSet()

        items?.forEach {
            val savedCritter = crittersService.get { crittersService.table.critter eq it.critter }

            if (savedCritter == null) crittersService.add(it)
        }
    }

    transaction {
        drop(
            FrozenFoodEffectTable,
            IngredientHeartsTable,
            CookingPotFoodIngredientsTable,
            CookingPotFoodsTable,
            OtherFoodEffectTable,
            ElixirsTable,
            FrozenFoodsTable,
            CrittersTable,
            FrozenFoodIngredientsTable,
            OtherFoodsTable,
            EffectsTable,
            MonsterPartsTable,
            RoastedFoodIngredientsTable,
            OtherFoodIngredientsTable,
            ImagesTable,
            IngredientBonusAddOnsTable,
            IngredientDurationTable,
            ElixirIngredientsTable,
            RoastedFoodsTable,
            IngredientsTable,
            RoastedFoodEffectTable
        )
        create(
            FrozenFoodEffectTable,
            IngredientHeartsTable,
            CookingPotFoodIngredientsTable,
            CookingPotFoodsTable,
            OtherFoodEffectTable,
            ElixirsTable,
            FrozenFoodsTable,
            CrittersTable,
            FrozenFoodIngredientsTable,
            OtherFoodsTable,
            EffectsTable,
            MonsterPartsTable,
            RoastedFoodIngredientsTable,
            OtherFoodIngredientsTable,
            ImagesTable,
            IngredientBonusAddOnsTable,
            IngredientDurationTable,
            ElixirIngredientsTable,
            RoastedFoodsTable,
            IngredientsTable,
            RoastedFoodEffectTable
        )
    }

    parseFileAsClass<IngredientsResponse>("ingredients")?.items?.forEach {
        ingredientsService.add(it)
    }
    parseFileAsClass<EffectsResponse>("effects")?.items?.forEach {
        effectsService.add(it)
    }

    seedCritters()
    parseFileAsClass<CookingPotFoodsResponse>("cookingPotFood")?.items?.forEach {
        cookingPotFoodsService.add(it)
    }
    parseFileAsClass<ElixirsResponse>("elixirs")?.items?.forEach {
        elixirsService.add(it)
    }
    parseFileAsClass<FrozenFoodsResponse>("frozenFood")?.items?.forEach {
        frozenFoodsService.add(it)
    }
    parseFileAsClass<MonsterPartsResponse>("monsterParts")?.items?.forEach {
        monsterPartsService.add(it)
    }
    parseFileAsClass<OtherFoodsResponse>("otherFood")?.items?.forEach {
        otherFoodsService.add(it)
    }
    parseFileAsClass<RoastedFoodsResponse>("roastedFood")?.items?.forEach {
        roastedFoodsService.add(it)
    }
}

private inline fun <reified T> parseFileAsClass(name: String) =
    File("breathOfTheWild/resources/$name.json").readText().fromJson<T>()
