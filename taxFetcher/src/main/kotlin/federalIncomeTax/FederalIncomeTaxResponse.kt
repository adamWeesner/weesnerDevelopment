package federalIncomeTax

import com.squareup.moshi.JsonClass
import generics.GenericResponse
import shared.taxFetcher.FederalIncomeTax

@JsonClass(generateAdapter = true)
data class FederalIncomeTaxResponse(
    override var items: List<FederalIncomeTax>? = null
) : GenericResponse<FederalIncomeTax>
