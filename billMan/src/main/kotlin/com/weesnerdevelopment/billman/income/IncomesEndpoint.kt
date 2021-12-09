package com.weesnerdevelopment.billman.income

import Path
import io.ktor.locations.*

@OptIn(KtorExperimentalLocationsAPI::class)
@Location(Path.BillMan.income)
data class IncomesEndpoint(val id: String? = null)