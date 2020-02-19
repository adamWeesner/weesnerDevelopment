package taxWithholding

import com.squareup.moshi.JsonClass
import generics.GenericResponse

@JsonClass(generateAdapter = true)
data class TaxWithholdingResponse(
    override var items: List<TaxWithholding>? = null
) : GenericResponse<TaxWithholding>
