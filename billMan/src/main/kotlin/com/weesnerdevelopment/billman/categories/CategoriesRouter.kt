package categories

import BaseRouter
import com.weesnerdevelopment.shared.base.GenericResponse
import com.weesnerdevelopment.shared.billMan.Category
import com.weesnerdevelopment.shared.billMan.responses.CategoriesResponse
import com.weesnerdevelopment.shared.toJson
import kotlin.reflect.full.createType

class CategoriesRouter(
    override val basePath: String,
    service: CategoriesService
) : BaseRouter<Category, CategoriesService>(
    CategoriesResponse(),
    service,
    Category::class.createType()
) {
    override fun GenericResponse<Category>.parse(): String = this.toJson()
}
