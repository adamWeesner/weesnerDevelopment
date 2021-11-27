package serialCabinet.category

import BaseRouter
import com.weesnerdevelopment.shared.base.GenericResponse
import com.weesnerdevelopment.shared.serialCabinet.Category
import com.weesnerdevelopment.shared.serialCabinet.responses.CategoriesResponse
import com.weesnerdevelopment.shared.toJson
import kotlin.reflect.full.createType

data class CategoriesRouter(
    override val basePath: String,
    override val service: CategoriesService
) : BaseRouter<Category, CategoriesService>(
    CategoriesResponse(),
    service,
    Category::class.createType()
) {
    override fun GenericResponse<Category>.parse(): String = this.toJson()
}
