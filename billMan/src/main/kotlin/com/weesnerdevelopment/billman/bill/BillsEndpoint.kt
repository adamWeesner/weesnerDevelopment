package com.weesnerdevelopment.billman.bill

import com.weesnerdevelopment.shared.Paths
import io.ktor.server.locations.*

@Location(Paths.BillMan.bills)
data class BillsEndpoint(val id: String? = null)