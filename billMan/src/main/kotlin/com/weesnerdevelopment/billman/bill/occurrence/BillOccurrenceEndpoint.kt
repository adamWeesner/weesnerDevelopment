package com.weesnerdevelopment.billman.bill.occurrence

import Path
import io.ktor.locations.*

@OptIn(KtorExperimentalLocationsAPI::class)
@Location(Path.BillMan.billOccurrences)
data class BillOccurrenceEndpoint(val id: String? = null)