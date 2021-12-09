package com.weesnerdevelopment.billman.category

import Path
import io.ktor.locations.*

@OptIn(KtorExperimentalLocationsAPI::class)
@Location(Path.BillMan.categories)
data class CategoriesEndpoint(val id: String? = null)