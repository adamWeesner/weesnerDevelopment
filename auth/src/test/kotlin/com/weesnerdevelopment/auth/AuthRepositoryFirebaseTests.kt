package com.weesnerdevelopment.auth

import com.weesnerdevelopment.auth.repository.AccountInfoException
import com.weesnerdevelopment.auth.repository.LoginException
import com.weesnerdevelopment.auth.repository.SignUpException
import com.weesnerdevelopment.auth.repository.UserRepository
import com.weesnerdevelopment.auth.repository.firebase.UserRepositoryFirebase
import com.weesnerdevelopment.shared.auth.User
import com.weesnerdevelopment.test.utils.shouldBe
import kimchi.Kimchi
import kimchi.logger.defaultWriter
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class FirebaseAuthTests {
    lateinit var authRepo: UserRepository

    @Before
    fun setup() {
        Kimchi.addLog(defaultWriter)
        authRepo = UserRepositoryFirebase
    }

    @Test
    fun `verify sign up with email of already signed up email`() = runBlocking {
        val created = authRepo.create(User(email = "testing@gmail.com", password = "randompassword"))
        created.exceptionOrNull() shouldBe SignUpException.EmailExists
    }

    @Test
    fun `verify sign in with email for signed up email`() = runBlocking {
        val signedInUser = authRepo.login("testing@gmail.com", "randompassword")
        signedInUser.getOrNull()?.email shouldBe "testing@gmail.com"
    }

    @Test
    fun `verify sign in with email for signed up email but invalid password`() = runBlocking {
        val signedInUser = authRepo.login("testing@gmail.com", "randompassword1")
        signedInUser.exceptionOrNull() shouldBe LoginException.InvalidPassword
    }

    @Test
    fun `verify sign in with email for not signed up email`() = runBlocking {
        val signedInUser = authRepo.login("testing2@gmail.com", "randompassword")
        signedInUser.exceptionOrNull() shouldBe LoginException.EmailNotFound
    }

    @Test
    fun `verify get user info`() = runBlocking {
        val signedInUser = authRepo.login("testing@gmail.com", "randompassword")
        val account = authRepo.account(signedInUser.getOrNull()?.authToken!!)
        account.getOrNull()?.email shouldBe "testing@gmail.com"
    }

    @Test
    fun `verify get user info for invalid user`() = runBlocking {
        val account = authRepo.account("")
        account.exceptionOrNull() shouldBe AccountInfoException.InvalidId
    }

    @Test
    fun `verify change email`() = runBlocking {
        val signedInUser = authRepo.login("testing@gmail.com", "randompassword").getOrNull()

        val updatedUser = authRepo.update(signedInUser?.copy(email = "testing2@gmail.com")!!)
        updatedUser.getOrNull()?.email shouldBe "testing2@gmail.com"

        val revertedUser = authRepo.update(signedInUser)
        revertedUser.getOrNull()?.email shouldBe "testing@gmail.com"
    }

    @Test
    fun `verify change password`(): Unit = runBlocking {
        val signedInUser = authRepo.login("testing@gmail.com", "randompassword").getOrNull()

        val updatedUser = authRepo.update(signedInUser?.copy(password = "randompassword1")!!)
        updatedUser.getOrNull()?.email shouldBe "testing@gmail.com"

        authRepo.update(signedInUser.copy(password = "randompassword"))
    }

    @Test
    fun `verify change displayName`() = runBlocking {
        val signedInUser = authRepo.login("testing@gmail.com", "randompassword").getOrNull()

        val updatedUser = authRepo.update(signedInUser?.copy(username = "random user")!!)
        updatedUser.getOrNull()?.username shouldBe "random user"

        val revertedUser = authRepo.update(signedInUser)
        revertedUser.getOrNull()?.username shouldBe ""
    }

    @Test
    fun `verify change photoUrl`() = runBlocking {
        val signedInUser = authRepo.login("testing@gmail.com", "randompassword").getOrNull()

        val updatedUser = authRepo.update(signedInUser?.copy(photoUrl = "randomUrl")!!)
        updatedUser.getOrNull()?.photoUrl shouldBe "randomUrl"

        val revertedUser = authRepo.update(signedInUser)
        revertedUser.getOrNull()?.photoUrl shouldBe null
    }

    @Test
    fun `verify delete account`() = runBlocking {
        val signedInUser = authRepo.login("testing@gmail.com", "randompassword").getOrNull()

        authRepo.delete(signedInUser?.authToken!!) shouldBe true

        val created = authRepo.create(User(email = "testing@gmail.com", password = "randompassword"))
        created.isSuccess shouldBe true
    }
}