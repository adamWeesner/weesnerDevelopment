package medicare

import com.squareup.moshi.JsonClass
import generics.GenericResponse
import shared.taxFetcher.Medicare

@JsonClass(generateAdapter = true)
data class MedicareResponse(
    override var items: List<Medicare>? = null
) : GenericResponse<Medicare>
