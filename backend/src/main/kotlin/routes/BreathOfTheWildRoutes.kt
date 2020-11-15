package com.weesnerdevelopment.routes

import breathOfTheWild.cookingPotFood.CookingPotFoodsRouter
import breathOfTheWild.critter.CrittersRouter
import breathOfTheWild.elixirs.ElixirsRouter
import breathOfTheWild.ingredients.IngredientsRouter
import io.ktor.auth.*
import io.ktor.routing.*
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein

fun Routing.breathOfTheWildRoutes() {
    val crittersRouter by kodein().instance<CrittersRouter>()
    val cookingPotFoodsRouter by kodein().instance<CookingPotFoodsRouter>()
    val ingredientsRouter by kodein().instance<IngredientsRouter>()
    val elixirsRouter by kodein().instance<ElixirsRouter>()

    crittersRouter.apply {
        authenticate {
            setupRoutes()
        }
    }

    cookingPotFoodsRouter.apply {
        authenticate {
            setupRoutes()
        }
    }

    ingredientsRouter.apply {
        authenticate {
            setupRoutes()
        }
    }

    elixirsRouter.apply {
        authenticate {
            setupRoutes()
        }
    }
}
