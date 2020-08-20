package categories

import BaseRouter
import shared.billMan.Category
import shared.billMan.responses.CategoriesResponse
import kotlin.reflect.full.createType

class CategoriesRouter(
    override val basePath: String,
    service: CategoriesService
) : BaseRouter<Category, CategoriesService>(
    CategoriesResponse(),
    service,
    Category::class.createType()
)
