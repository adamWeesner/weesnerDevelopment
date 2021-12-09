package com.weesnerdevelopment.billman.income.occurrence

import Path
import io.ktor.locations.*

@OptIn(KtorExperimentalLocationsAPI::class)
@Location(Path.BillMan.incomeOccurrences)
data class IncomeOccurrenceEndpoint(val id: String? = null)