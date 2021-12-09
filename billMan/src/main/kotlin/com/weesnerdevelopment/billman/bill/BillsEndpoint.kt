package com.weesnerdevelopment.billman.bill

import Path
import io.ktor.locations.*

@OptIn(KtorExperimentalLocationsAPI::class)
@Location(Path.BillMan.bills)
data class BillsEndpoint(val id: String? = null)