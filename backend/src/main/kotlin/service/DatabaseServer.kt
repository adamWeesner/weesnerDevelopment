package com.weesnerdevelopment.service

import category.CategoriesResponse
import category.CategoriesService
import com.ryanharter.ktor.moshi.moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.weesnerdevelopment.Paths.*
import federalIncomeTax.FederalIncomeTaxResponse
import federalIncomeTax.FederalIncomeTaxService
import generics.route
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.routing.Routing
import io.ktor.websocket.WebSockets
import medicare.MedicareResponse
import medicare.MedicareService
import socialSecurity.SocialSecurityResponse
import socialSecurity.SocialSecurityService
import taxWithholding.TaxWithholdingResponse
import taxWithholding.TaxWithholdingService
import java.time.Duration

class DatabaseServer {
    fun Application.main() {
        install(DefaultHeaders)
        install(CallLogging)
        install(WebSockets)
        install(CORS) {
            allowCredentials = true
            host("weesnerdevelopment.com", subDomains = listOf("api"))
            host("localhost:3000")
            maxAge = Duration.ofDays(1)
            allowNonSimpleContentTypes = true
        }

        install(ContentNegotiation) {
            moshi {
                add(KotlinJsonAdapterFactory())
            }
        }

        DatabaseFactory.init()

        install(Routing) {
            // tax fetcher
            route(socialSecurity.name, SocialSecurityService()) { SocialSecurityResponse(it) }
            route(medicare.name, MedicareService()) { MedicareResponse(it) }
            route(taxWithholding.name, TaxWithholdingService()) { TaxWithholdingResponse(it) }
            route(federalIncomeTax.name, FederalIncomeTaxService()) { FederalIncomeTaxResponse(it) }
            // bill man
            route(category.name, CategoriesService()) { CategoriesResponse(it) }
        }
    }
}