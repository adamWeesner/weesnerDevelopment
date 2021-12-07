package com.weesnerdevelopment.billMan.updated

import Path
import com.weesnerdevelopment.billman.redux.*
import com.weesnerdevelopment.test.utils.fromFile
import com.weesnerdevelopment.test.utils.handleRequest
import com.weesnerdevelopment.test.utils.shouldBe
import com.weesnerdevelopment.test.utils.testApp
import io.ktor.http.*
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TestUserRoutes {
    private val baseUrl = Path.User.basePath
    private val config = "application-test.conf"

    sealed class Testing {
        @AfterEach
        fun cleanUp() {
            transaction {
                SchemaUtils.drop(
                    UserTable,
                    BillTable,
                    ColorTable,
                    CategoryTable,
                    BillsCategoriesTable,
                    BillSharedUsersTable
                )
                SchemaUtils.create(
                    UserTable,
                    BillTable,
                    ColorTable,
                    CategoryTable,
                    BillsCategoriesTable,
                    BillSharedUsersTable
                )
            }
        }
    }

    @Nested
    @DisplayName("account")
    inner class Account : Testing() {
        @Test
        fun `get user account for user that is not in the database`() = testApp(config) {
            val call = handleRequest(Get, "$baseUrl?id=4f493b2d-1b86-40de-8710-6bfb5032f1e2")

            call.response.status() shouldBe HttpStatusCode.NotFound
            call.response.content shouldBe fromFile(baseUrl, "getAccountIdNotFoundResponse")
        }

        @Test
        fun `get user account with invalid id`() = testApp(config) {
            val call = handleRequest(Get, "$baseUrl?id=a")

            call.response.content shouldBe fromFile(baseUrl, "getAccountIdInvalidResponse")
        }

        @Test
        fun `get user account that is in the database`() = testApp(config) {
            val add = handleRequest(Post, baseUrl, "createUserValidRequestBody")
            add.response.status() shouldBe HttpStatusCode.Created

            val call = handleRequest(Get, "$baseUrl?id=4f493b2d-1b86-40de-8710-6bfb5032f1e3")

            call.response.status() shouldBe HttpStatusCode.OK
        }
    }

}