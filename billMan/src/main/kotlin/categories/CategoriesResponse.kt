package categories

import com.squareup.moshi.JsonClass
import generics.GenericResponse
import shared.billMan.Category

@JsonClass(generateAdapter = true)
data class CategoriesResponse(
    override var items: List<Category>? = null
) : GenericResponse<Category>
