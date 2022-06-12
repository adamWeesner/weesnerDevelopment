package com.weesnerdevelopment.auth.repository.firebase

internal sealed class FirebaseEndpoint(endpoint: String) {
    private val apiKey = System.getenv("firebaseApiKey").also {
        println("api key is $it")
    }

    val url = "https://identitytoolkit.googleapis.com/v1/accounts:$endpoint?key=$apiKey"
}

internal object SignUpEmail : FirebaseEndpoint("signUp")
internal object SignInEmail : FirebaseEndpoint("signInWithPassword")
internal object GetUserInfo : FirebaseEndpoint("lookup")
internal object ChangeInfo : FirebaseEndpoint("update")
internal object DeleteAccount : FirebaseEndpoint("delete")
