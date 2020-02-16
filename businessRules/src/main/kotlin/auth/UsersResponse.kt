package auth

import com.squareup.moshi.JsonClass
import generics.GenericResponse

@JsonClass(generateAdapter = true)
data class UsersResponse(
    override var items: List<User>? = null
) : GenericResponse<User>(items)
