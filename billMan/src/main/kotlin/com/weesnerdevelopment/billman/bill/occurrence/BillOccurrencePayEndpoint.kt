package com.weesnerdevelopment.billman.bill.occurrence

import com.weesnerdevelopment.shared.Paths
import io.ktor.locations.*

@OptIn(KtorExperimentalLocationsAPI::class)
@Location("${Paths.BillMan.billOccurrences}/pay")
data class BillOccurrencePayEndpoint(val id: String, val payment: String)