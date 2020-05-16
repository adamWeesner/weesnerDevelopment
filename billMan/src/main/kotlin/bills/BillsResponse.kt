package bills

import com.squareup.moshi.JsonClass
import generics.GenericResponse
import shared.billMan.Bill

@JsonClass(generateAdapter = true)
data class BillsResponse(
    override var items: List<Bill>? = null
) : GenericResponse<Bill>
