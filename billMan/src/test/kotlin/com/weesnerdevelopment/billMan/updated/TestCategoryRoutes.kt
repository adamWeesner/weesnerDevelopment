package com.weesnerdevelopment.billMan.updated

import Path
import com.weesnerdevelopment.billman.redux.*
import com.weesnerdevelopment.shared.auth.TokenResponse
import com.weesnerdevelopment.shared.fromJson
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

class TestCategoryRoutes {
    private val baseUrl = Path.BillMan.categories
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
    @DisplayName("get all")
    inner class GetAll : Testing() {
        @Test
        fun `get all with no categories returns empty`() = testApp(config) {
            val token = handleRequest(Post, Path.User.basePath, "createUserValidRequestBody")
                .response.content?.fromJson<TokenResponse>()?.token

            val call = handleRequest(Get, baseUrl, bearerToken = token)

            call.response.status() shouldBe HttpStatusCode.OK
            call.response.content shouldBe fromFile(baseUrl, "getCategoriesEmptyResponse")
        }

        @Test
        fun `get all with 1 event returns categories list`() = testApp(config) {
            val token = handleRequest(Post, Path.User.basePath, "createUserValidRequestBody")
                .response.content?.fromJson<TokenResponse>()?.token

            val add = handleRequest(Post, baseUrl, "addCategoryValidRequestBodyNoOwner", token)
            add.response.status() shouldBe HttpStatusCode.Created

            val call = handleRequest(Get, baseUrl, bearerToken = token)

            call.response.status() shouldBe HttpStatusCode.OK
            call.response.content shouldBe fromFile(baseUrl, "getCategories1ItemResponseNoOwner")
        }
    }

    @Nested
    @DisplayName("get single")
    inner class GetSingle : Testing() {
        @Test
        fun `get single category that is not in the database`() = testApp(config) {
            val token = handleRequest(Post, Path.User.basePath, "createUserValidRequestBody")
                .response.content?.fromJson<TokenResponse>()?.token

            val call = handleRequest(Get, "$baseUrl?id=4f982b2d-1b86-40de-8710-6bfb4649f1e3", bearerToken = token)

            call.response.status() shouldBe HttpStatusCode.NotFound
            call.response.content shouldBe fromFile(baseUrl, "getCategoryIdNotFoundResponse")
        }

        @Test
        fun `get single category with invalid id`() = testApp(config) {
            val token = handleRequest(Post, Path.User.basePath, "createUserValidRequestBody")
                .response.content?.fromJson<TokenResponse>()?.token

            val call = handleRequest(Get, "$baseUrl?id=a", bearerToken = token)

            call.response.status() shouldBe HttpStatusCode.BadRequest
            call.response.content shouldBe fromFile(baseUrl, "getCategoryIdInvalidResponse")
        }

        @Test
        fun `get single category that is in the database`() = testApp(config) {
            val token = handleRequest(Post, Path.User.basePath, "createUserValidRequestBody")
                .response.content?.fromJson<TokenResponse>()?.token

            val add = handleRequest(Post, baseUrl, "addCategoryValidRequestBodyNoOwner", token)
            add.response.status() shouldBe HttpStatusCode.Created

            val call = handleRequest(Get, "$baseUrl?id=4f982b2d-1b86-40de-8710-6bfb4649f1e3", bearerToken = token)

            call.response.status() shouldBe HttpStatusCode.OK
            call.response.content shouldBe fromFile(baseUrl, "getCategoryResponseNoOwner")
        }
    }

}