package income

import com.squareup.moshi.JsonClass
import generics.GenericResponse
import shared.billMan.Income

@JsonClass(generateAdapter = true)
data class IncomeResponse(
    override var items: List<Income>? = null
) : GenericResponse<Income>
