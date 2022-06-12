package com.weesnerdevelopment.billman.income.occurrence

import com.weesnerdevelopment.shared.Paths
import io.ktor.server.locations.*

@Location(Paths.BillMan.incomeOccurrences)
data class IncomeOccurrenceEndpoint(val id: String? = null)