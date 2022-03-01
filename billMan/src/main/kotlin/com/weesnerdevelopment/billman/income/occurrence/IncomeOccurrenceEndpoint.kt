package com.weesnerdevelopment.billman.income.occurrence

import com.weesnerdevelopment.shared.Paths
import io.ktor.locations.*

@OptIn(KtorExperimentalLocationsAPI::class)
@Location(Paths.BillMan.incomeOccurrences)
data class IncomeOccurrenceEndpoint(val id: String? = null)