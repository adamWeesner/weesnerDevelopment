package com.weesnerdevelopment.routes

import federalIncomeTax.FederalIncomeTaxRouter
import generics.route
import io.ktor.auth.authenticate
import io.ktor.routing.Routing
import medicare.MedicareRouter
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein
import socialSecurity.SocialSecurityRouter
import taxWithholding.TaxWithholdingRouter

fun Routing.taxFetcherRoutes() {
    val federalIncomeTaxRouter by kodein().instance<FederalIncomeTaxRouter>()
    val medicareRouter by kodein().instance<MedicareRouter>()
    val socialSecurityRouter by kodein().instance<SocialSecurityRouter>()
    val taxWithholdingRouter by kodein().instance<TaxWithholdingRouter>()

    authenticate {
        route(federalIncomeTaxRouter)
        route(medicareRouter)
        route(socialSecurityRouter)
        route(taxWithholdingRouter)
    }
}
