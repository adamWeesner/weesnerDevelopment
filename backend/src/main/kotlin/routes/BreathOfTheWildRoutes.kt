package com.weesnerdevelopment.routes

import breathOfTheWild.cookingPotFood.CookingPotFoodsRouter
import breathOfTheWild.critter.CrittersRouter
import breathOfTheWild.effect.EffectsRouter
import breathOfTheWild.elixir.ElixirsRouter
import breathOfTheWild.frozenFood.FrozenFoodsRouter
import breathOfTheWild.ingredient.IngredientsRouter
import breathOfTheWild.monsterPart.MonsterPartsRouter
import breathOfTheWild.otherFood.OtherFoodsRouter
import breathOfTheWild.roastedFood.RoastedFoodsRouter
import io.ktor.auth.*
import io.ktor.routing.*
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein

fun Routing.breathOfTheWildRoutes() {
    val cookingPotFoodsRouter by kodein().instance<CookingPotFoodsRouter>()
    val elixirsRouter by kodein().instance<ElixirsRouter>()
    val frozenFoodsRouter by kodein().instance<FrozenFoodsRouter>()
    val crittersRouter by kodein().instance<CrittersRouter>()
    val otherFoodsRouter by kodein().instance<OtherFoodsRouter>()
    val effectsRouter by kodein().instance<EffectsRouter>()
    val monsterPartsRouter by kodein().instance<MonsterPartsRouter>()
    val roastedFoodsRouter by kodein().instance<RoastedFoodsRouter>()
    val ingredientsRouter by kodein().instance<IngredientsRouter>()

    cookingPotFoodsRouter.apply {
        authenticate {
            setupRoutes()
        }
    }

    elixirsRouter.apply {
        authenticate {
            setupRoutes()
        }
    }

    frozenFoodsRouter.apply {
        authenticate {
            setupRoutes()
        }
    }

    crittersRouter.apply {
        authenticate {
            setupRoutes()
        }
    }

    otherFoodsRouter.apply {
        authenticate {
            setupRoutes()
        }
    }

    effectsRouter.apply {
        authenticate {
            setupRoutes()
        }
    }

    monsterPartsRouter.apply {
        authenticate {
            setupRoutes()
        }
    }

    roastedFoodsRouter.apply {
        authenticate {
            setupRoutes()
        }
    }

    ingredientsRouter.apply {
        authenticate {
            setupRoutes()
        }
    }
}
        