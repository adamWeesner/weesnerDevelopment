package com.weesnerdevelopment.auth.repository

sealed class AuthException : Throwable()

sealed class SignUpException : AuthException() {
    object EmailExists : SignUpException()
}

sealed class LoginException : AuthException() {
    object EmailNotFound : LoginException()
    object InvalidPassword : LoginException()
}

sealed class AccountInfoException : AuthException() {
    object InvalidId : AccountInfoException()
}

sealed class UpdateInfoException : AuthException() {
    object NoUpdates : UpdateInfoException()
    object Email : UpdateInfoException()
    object Password : UpdateInfoException()
    object Other : UpdateInfoException()
    class MultipleFailure(vararg exceptions: UpdateInfoException) : UpdateInfoException()
}
