package com.weesnerdevelopment.billMan

import Path
import com.weesnerdevelopment.auth.user.UserTable
import com.weesnerdevelopment.billman.bill.BillSharedUsersTable
import com.weesnerdevelopment.billman.bill.BillTable
import com.weesnerdevelopment.billman.bill.BillsCategoriesTable
import com.weesnerdevelopment.billman.category.CategoryTable
import com.weesnerdevelopment.billman.color.ColorTable
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

class TestBillRoutes {
    private val baseUrl = Path.BillMan.bills
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
        fun `get all with no bills returns empty`() = testApp(config) {
            val token = handleRequest(Post, Path.User.basePath, "createUserValidRequestBody")
                .response.content?.fromJson<TokenResponse>()?.token

            val call = handleRequest(Get, baseUrl, bearerToken = token)

            call.response.status() shouldBe HttpStatusCode.OK
            call.response.content shouldBe fromFile(baseUrl, "getBillsEmptyResponse")
        }

        @Test
        fun `get all with 1 bill returns bills list`() = testApp(config) {
            val token = handleRequest(Post, Path.User.basePath, "createUserValidRequestBody")
                .response.content?.fromJson<TokenResponse>()?.token

            val addCat = handleRequest(Post, Path.BillMan.categories, "addCategoryValidRequestBodyNoOwner", token)
            addCat.response.status() shouldBe HttpStatusCode.Created

            val add = handleRequest(Post, baseUrl, "addBillValidRequestBody", token)
            add.response.status() shouldBe HttpStatusCode.Created

            val call = handleRequest(Get, baseUrl, bearerToken = token)

            call.response.status() shouldBe HttpStatusCode.OK
            call.response.content shouldBe fromFile(baseUrl, "getBills1ItemResponse")
        }
    }

    @Nested
    @DisplayName("get single")
    inner class GetSingle : Testing() {
        @Test
        fun `get single bill that is not in the database`() = testApp(config) {
            val token = handleRequest(Post, Path.User.basePath, "createUserValidRequestBody")
                .response.content?.fromJson<TokenResponse>()?.token

            val call = handleRequest(Get, "$baseUrl?id=4f493b2d-1b86-40de-8710-6bfb5032f1e2", bearerToken = token)

            call.response.content shouldBe fromFile(baseUrl, "getBillIdNotFoundResponse")
        }

        @Test
        fun `get single bill with invalid id`() = testApp(config) {
            val token = handleRequest(Post, Path.User.basePath, "createUserValidRequestBody")
                .response.content?.fromJson<TokenResponse>()?.token

            val call = handleRequest(Get, "$baseUrl?id=a", bearerToken = token)

            call.response.content shouldBe fromFile(baseUrl, "getBillIdInvalidResponse")
        }

        @Test
        fun `get single bill that is in the database`() = testApp(config) {
            val token = handleRequest(Post, Path.User.basePath, "createUserValidRequestBody")
                .response.content?.fromJson<TokenResponse>()?.token

            val addCat = handleRequest(Post, Path.BillMan.categories, "addCategoryValidRequestBodyNoOwner", token)
            addCat.response.status() shouldBe HttpStatusCode.Created

            val add = handleRequest(Post, baseUrl, "addBillValidRequestBody", token)
            add.response.status() shouldBe HttpStatusCode.Created

            val call = handleRequest(Get, "$baseUrl?id=4f982b2d-1b86-40de-8710-6bfb4649f1e4", bearerToken = token)

            call.response.content shouldBe fromFile(baseUrl, "getBillResponseSuccess")
        }
    }
}