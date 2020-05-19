package occurrences

import com.squareup.moshi.JsonClass
import generics.GenericResponse
import shared.billMan.Occurrence

@JsonClass(generateAdapter = true)
data class OccurrencesResponse(
    override var items: List<Occurrence>? = null
) : GenericResponse<Occurrence>
