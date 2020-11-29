package serialCabinet.category

import BaseRouter
import shared.serialCabinet.Category
import shared.serialCabinet.responses.CategoriesResponse
import kotlin.reflect.full.createType

data class CategoriesRouter(
    override val basePath: String,
    override val service: CategoriesService
) : BaseRouter<Category, CategoriesService>(
    CategoriesResponse(),
    service,
    Category::class.createType()
)
