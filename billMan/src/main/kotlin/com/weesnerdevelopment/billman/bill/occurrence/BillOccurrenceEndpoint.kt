package com.weesnerdevelopment.billman.bill.occurrence

import com.weesnerdevelopment.shared.Paths
import io.ktor.server.locations.*

@Location(Paths.BillMan.billOccurrences)
data class BillOccurrenceEndpoint(val id: String? = null)