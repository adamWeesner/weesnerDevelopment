package com.weesnerdevelopment.billman.bill.occurrence

import com.weesnerdevelopment.shared.Paths
import io.ktor.server.locations.*

@Location("${Paths.BillMan.billOccurrences}/pay")
data class BillOccurrencePayEndpoint(val id: String, val payment: String)