package com.weesnerdevelopment.billman.income

import com.weesnerdevelopment.shared.Paths
import io.ktor.server.locations.*

@Location(Paths.BillMan.income)
data class IncomesEndpoint(val id: String? = null)