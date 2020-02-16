package federalIncomeTax

import com.squareup.moshi.JsonClass
import generics.GenericResponse

@JsonClass(generateAdapter = true)
data class FederalIncomeTaxResponse(
    override var items: List<FederalIncomeTax>? = null
) : GenericResponse<FederalIncomeTax>(items)
