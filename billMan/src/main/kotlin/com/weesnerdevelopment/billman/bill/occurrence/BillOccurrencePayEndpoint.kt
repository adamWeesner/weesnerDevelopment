package com.weesnerdevelopment.billman.bill.occurrence

import Path
import io.ktor.locations.*

@OptIn(KtorExperimentalLocationsAPI::class)
@Location("${Path.BillMan.billOccurrences}/pay")
data class BillOccurrencePayEndpoint(val id: String, val payment: String)