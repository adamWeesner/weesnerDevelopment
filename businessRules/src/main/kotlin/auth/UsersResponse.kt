package auth

import com.squareup.moshi.JsonClass
import generics.GenericResponse
import shared.auth.User

@JsonClass(generateAdapter = true)
data class UsersResponse(
    override var items: List<User>? = null
) : GenericResponse<User>
