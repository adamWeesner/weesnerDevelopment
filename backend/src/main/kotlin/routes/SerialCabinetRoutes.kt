package com.weesnerdevelopment.routes

import io.ktor.auth.*
import io.ktor.routing.*
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein
import serialCabinet.category.CategoriesRouter
import serialCabinet.electronic.ElectronicsRouter
import serialCabinet.manufacturer.ManufacturersRouter

fun Routing.serialCabinetRoutes() {
    val categoriesRouter by kodein().instance<CategoriesRouter>()
    val electronicsRouter by kodein().instance<ElectronicsRouter>()
    val manufacturersRouter by kodein().instance<ManufacturersRouter>()

    categoriesRouter.apply {
        authenticate {
            setupRoutes()
        }
    }

    electronicsRouter.apply {
        authenticate {
            setupRoutes()
        }
    }

    manufacturersRouter.apply {
        authenticate {
            setupRoutes()
        }
    }
}
