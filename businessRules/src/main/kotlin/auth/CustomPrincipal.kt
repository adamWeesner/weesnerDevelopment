package auth

import com.auth0.jwt.interfaces.Payload
import io.ktor.auth.Principal

/**
 * Principal for WeesnerDevelopment that gets the attr fields from the Jwt token.
 */
data class CustomPrincipal(val data: Payload?) : Principal {
    /**
     * Pulls the data from the [Principal].
     *
     * @return [PrincipalData].
     */
    fun getData() = data?.claims?.run {
        val items = mutableMapOf<String, String?>()

        forEach {
            if (it.key.startsWith("attr-"))
                items[it.key.replace("attr-", "")] = it.value?.asString()
        }

        val username = items["username"]
        val password = items["password"]
        val uuid = items["uuid"]

        PrincipalData(username, password, uuid)
    }
}

/**
 * Class for the attr fields from the jwt token.
 */
data class PrincipalData(val username: String?, val password: String?, val uuid: String?)

