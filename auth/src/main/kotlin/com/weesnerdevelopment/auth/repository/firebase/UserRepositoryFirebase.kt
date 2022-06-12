package com.weesnerdevelopment.auth.repository.firebase

import com.weesnerdevelopment.auth.repository.*
import com.weesnerdevelopment.auth.repository.firebase.request.*
import com.weesnerdevelopment.auth.repository.firebase.response.*
import com.weesnerdevelopment.businessRules.Log
import com.weesnerdevelopment.shared.auth.TokenUser
import com.weesnerdevelopment.shared.auth.User
import com.weesnerdevelopment.shared.fromJson
import com.weesnerdevelopment.shared.toJson
import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import java.net.ConnectException

object UserRepositoryFirebase : UserRepository {
    private val httpClient = HttpClient(Java) {
        expectSuccess = false
    }

    override suspend fun info(id: String): Result<User> {
        return account(id)
    }

    override suspend fun account(id: String): Result<User> {
        Log.info("Attempting to get user info")
        val response = httpClient.post(GetUserInfo.url) {
            setBody(UserInfoRequest(id).toJson())
        }

        val responseBody = response.bodyAsText()

        Log.debug("response from attempting login to account is $response")

        runCatching {
            val userFromResponse = with(responseBody.fromJson<UserInfoResponse>().users.first()) {
                User(
                    uuid = localId,
                    email = email,
                    username = displayName,
                    photoUrl = photoUrl,
                    dateCreated = createdAt.toLong()
                )
            }
            Log.info("Successfully retrieved user info")
            return Result.success(userFromResponse)
        }.getOrElse {
            Log.debug("Failed to parse as a login response... $it")
            return when (it) {
                is ConnectException -> {
                    Result.failure(it)
                }
                else -> {
                    val responseError = responseBody.fromJson<FirebaseAuthException>()
                    return when (responseError.error.message) {
                        "INVALID_ID_TOKEN" -> Result.failure(AccountInfoException.InvalidId)
                        else -> Result.failure(it)
                    }
                }
            }
        }
    }

    override suspend fun create(new: User): Result<TokenUser> {
        Log.info("Attempting to create new account")
        val response = httpClient.post(SignUpEmail.url) {
            setBody(new.toJson())
        }

        val responseBody = response.bodyAsText()

        Log.debug("response from attempting create account is $response")
        runCatching {
            val userFromResponse = with(responseBody.fromJson<SignUpResponse>()) {
                TokenUser(
                    uuid = localId,
                    email = email,
                    username = email,
                    refreshToken = refreshToken,
                    expiresIn = expiresIn.toLong(),
                    authToken = idToken
                )
            }
            return Result.success(userFromResponse)
        }.getOrElse {
            Log.debug("Failed to parse as a signup response... $it")
            return when (it) {
                is ConnectException -> {
                    Result.failure(it)
                }
                else -> {
                    val responseError = responseBody.fromJson<FirebaseAuthException>()
                    return when (responseError.error.message) {
                        "EMAIL_EXISTS" -> Result.failure(SignUpException.EmailExists)
                        else -> Result.failure(it)
                    }
                }
            }
        }
    }

    override suspend fun login(email: String, password: String): Result<TokenUser> {
        Log.info("Attempting to login to user account")
        val response = httpClient.post(SignInEmail.url) {
            setBody(SignInRequest(email, password).toJson())
        }

        val responseBody = response.bodyAsText()

        Log.debug("response from attempting login to account is $response")
        runCatching {
            val userFromResponse = with(responseBody.fromJson<SignInResponse>()) {
                TokenUser(
                    uuid = localId,
                    email = email,
                    username = displayName,
                    expiresIn = 0L,
                    refreshToken = "",
                    authToken = idToken
                )
            }
            return Result.success(userFromResponse)
        }.getOrElse {
            Log.debug("Failed to parse as a login response... $it")
            return when (it) {
                is ConnectException -> {
                    Result.failure(it)
                }
                else -> {
                    val responseError = responseBody.fromJson<FirebaseAuthException>()
                    return when (responseError.error.message) {
                        "INVALID_PASSWORD" -> Result.failure(LoginException.InvalidPassword)
                        "EMAIL_NOT_FOUND" -> Result.failure(LoginException.EmailNotFound)
                        else -> Result.failure(it)
                    }
                }
            }
        }
    }

    override suspend fun update(updated: TokenUser): Result<TokenUser> {
        var currentAuthToken = updated.authToken
        var updatePasswordResult: Result<UpdateInfoResponse>? = null
        var updateEmailResult: Result<UpdateInfoResponse>? = null
        var updateOtherInfoResult: Result<UpdateInfoResponse>? = null

        val userInfo = account(updated.authToken).getOrNull()

        // if password changed update down that flow
        val updatedPassword = updated.password
        if (!updatedPassword.isNullOrBlank()) {
            Log.info("Attempting to change password.")

            val response = httpClient.post(ChangeInfo.url) {
                setBody(ChangePasswordRequest(updated.authToken, updatedPassword).toJson())
            }

            val responseBody = response.bodyAsText()

            Log.debug("response from attempting update users password $response")
            runCatching {
                val updatedInfo = responseBody.fromJson<UpdateInfoResponse>()
                currentAuthToken = updatedInfo.idToken ?: updated.authToken
                updatePasswordResult = Result.success(updatedInfo)
            }.getOrElse {
                Log.debug("Failed to parse as update user password response... $it")
                updatePasswordResult = when (it) {
                    is ConnectException -> {
                        Result.failure(UpdateInfoException.Password)
                    }
                    else -> {
                        val responseError = responseBody.fromJson<FirebaseAuthException>()
                        when (responseError.error.message) {
                            else -> Result.failure(UpdateInfoException.Password)
                        }
                    }
                }
            }
        }

        // if email changed update down that flow
        val updatedEmail = updated.email
        if (updated.email != userInfo?.email && !updatedEmail.isNullOrBlank()) {
            Log.info("Attempting to change email.")

            val response = httpClient.post(ChangeInfo.url) {
                setBody(ChangeEmailRequest(currentAuthToken, updatedEmail).toJson())
            }

            val responseBody = response.bodyAsText()

            Log.debug("response from attempting update users email $response")
            runCatching {
                val updatedInfo = responseBody.fromJson<UpdateInfoResponse>()
                currentAuthToken = updatedInfo.idToken ?: updated.authToken
                updateEmailResult = Result.success(updatedInfo)
            }.getOrElse {
                Log.debug("Failed to parse as update user email response... $it")
                updateEmailResult = when (it) {
                    is ConnectException -> {
                        Result.failure(UpdateInfoException.Email)
                    }
                    else -> {
                        val responseError = responseBody.fromJson<FirebaseAuthException>()
                        when (responseError.error.message) {
                            else -> Result.failure(UpdateInfoException.Email)
                        }
                    }
                }
            }
        }

        // if username or photo url info changed update down that flow
        val usernameChanged = userInfo?.username != updated.username
        val photoUrlChanged = userInfo?.photoUrl != updated.photoUrl
        if (usernameChanged || photoUrlChanged) {
            Log.info("Attempting to update user information.")

            val deleteUsername =
                if (updated.username == null) listOf(ProfileAttributeToDelete.DISPLAY_NAME.name) else null
            val deletePhotoUrl =
                if (updated.photoUrl == null) listOf(ProfileAttributeToDelete.PHOTO_URL.name) else null

            val response = httpClient.post(ChangeInfo.url) {
                setBody(
                    ChangeProfileRequest(
                        idToken = currentAuthToken,
                        displayName = updated.username,
                        photoUrl = updated.photoUrl,
                        deleteAttribute = deleteUsername.orEmpty() + deletePhotoUrl.orEmpty()
                    ).toJson()
                )
            }

            val responseBody = response.bodyAsText()

            Log.debug("response from attempting update users other info $response")
            runCatching {
                val updatedInfo = responseBody.fromJson<UpdateInfoResponse>()
                currentAuthToken = updatedInfo.idToken ?: updated.authToken
                updateOtherInfoResult = Result.success(updatedInfo)
            }.getOrElse {
                Log.debug("Failed to parse as update users other info response... $it")
                updateOtherInfoResult = when (it) {
                    is ConnectException -> {
                        Result.failure(UpdateInfoException.Other)
                    }
                    else -> {
                        val responseError = responseBody.fromJson<FirebaseAuthException>()
                        when (responseError.error.message) {
                            else -> Result.failure(UpdateInfoException.Other)
                        }
                    }
                }
            }
        }

        // join and return the value
        if (updatePasswordResult == null && updateEmailResult == null && updateOtherInfoResult == null)
            return Result.failure(UpdateInfoException.NoUpdates)

        val exceptions = listOfNotNull(
            updatePasswordResult?.exceptionOrNull() as? UpdateInfoException,
            updateEmailResult?.exceptionOrNull() as? UpdateInfoException,
            updateOtherInfoResult?.exceptionOrNull() as? UpdateInfoException
        )

        when {
            exceptions.isEmpty() -> {
                val updateInfo = account(currentAuthToken)
                if (updateInfo.isFailure)
                    return Result.failure(updateInfo.exceptionOrNull() ?: UpdateInfoException.Other)

                val updatedUserInfo = updateInfo.getOrNull()?.let {
                    TokenUser(
                        id = it.id,
                        uuid = it.uuid,
                        email = it.email,
                        photoUrl = it.photoUrl,
                        username = it.username,
                        password = it.password,
                        authToken = currentAuthToken,
                        refreshToken = updated.refreshToken,
                        expiresIn = updated.expiresIn,
                        history = it.history,
                        dateCreated = it.dateCreated,
                        dateUpdated = it.dateUpdated
                    )
                }

                if (updatedUserInfo == null)
                    return Result.failure(UpdateInfoException.Other)

                return Result.success(updatedUserInfo)
            }
            exceptions.size == 1 ->
                return Result.failure(exceptions.first())
            else ->
                return Result.failure(UpdateInfoException.MultipleFailure(*exceptions.toTypedArray()))
        }
    }

    override suspend fun delete(id: String): Boolean {
        Log.info("Attempting to delete user ingo")
        val response = httpClient.post(DeleteAccount.url) {
            setBody(UserInfoRequest(id).toJson())
        }

        val responseBody = response.bodyAsText()

        Log.debug("response from attempting delete account is $response")
        runCatching {
            responseBody.fromJson<DeleteResponse>()
            Log.info("Successfully deleted user info")
            return true
        }.getOrElse {
            Log.debug("Failed to parse as a delete response... $it")
            return when (it) {
                is ConnectException ->
                    false
                else -> {
                    val responseError = responseBody.fromJson<FirebaseAuthException>()
                    return when (responseError.error.message) {
                        else -> false
                    }
                }
            }
        }
    }
}