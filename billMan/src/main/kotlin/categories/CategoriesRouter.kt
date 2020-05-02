package categories

import generics.GenericRouter
import shared.billMan.Category

class CategoriesRouter(
    basePath: String,
    categoriesService: CategoriesService
) : GenericRouter<Category, CategoriesTable>(
    basePath,
    categoriesService,
    CategoriesResponse()
)
