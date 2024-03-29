package com.weesnerdevelopment.billman.category

import com.weesnerdevelopment.shared.Paths
import io.ktor.server.locations.*

@Location(Paths.BillMan.categories)
data class CategoriesEndpoint(val id: String? = null)