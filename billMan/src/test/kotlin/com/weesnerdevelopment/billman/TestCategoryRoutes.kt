package com.weesnerdevelopment.billman

import com.weesnerdevelopment.shared.Paths
import com.weesnerdevelopment.test.utils.fromFile
import com.weesnerdevelopment.test.utils.handleRequest
import com.weesnerdevelopment.test.utils.shouldBe
import com.weesnerdevelopment.test.utils.testApp
import io.ktor.http.*
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TestCategoryRoutes : BillManTests() {
    override val baseUrl = Paths.BillMan.categories

    @Nested
    @DisplayName("get all")
    inner class GetAll : Testing() {
        @Test
        fun `get all with no categories returns empty`() = testApp(config) { token ->
            val call = handleRequest(Get, baseUrl, bearerToken = token)

            call.response.status() shouldBe HttpStatusCode.OK
            call.response.content shouldBe fromFile(baseUrl, "get/emptyResponse")
        }

        @Test
        fun `get all with 1 event returns categories list`() = testApp(config) { token ->
            val add = handleRequest(Post, baseUrl, "add/validRequestBodyNoOwner", token)
            add.response.status() shouldBe HttpStatusCode.Created

            val call = handleRequest(Get, baseUrl, bearerToken = token)

            call.response.status() shouldBe HttpStatusCode.OK
            call.response.content shouldBe fromFile(baseUrl, "get/singleItemResponseNoOwner")
        }
    }

    @Nested
    @DisplayName("get single")
    inner class GetSingle : Testing() {
        @Test
        fun `get single category that is not in the database`() = testApp(config) { token ->
            val call = handleRequest(Get, "$baseUrl?id=4f982b2d-1b86-40de-8710-6bfb4649f1e3", bearerToken = token)

            call.response.status() shouldBe HttpStatusCode.NotFound
            call.response.content shouldBe fromFile(baseUrl, "get/idNotFoundResponse")
        }

        @Test
        fun `get single category with invalid id`() = testApp(config) { token ->
            val call = handleRequest(Get, "$baseUrl?id=a", bearerToken = token)

            call.response.status() shouldBe HttpStatusCode.BadRequest
            call.response.content shouldBe fromFile(baseUrl, "get/idInvalidResponse")
        }

        @Test
        fun `get single category that is in the database`() = testApp(config) { token ->
            val add = handleRequest(Post, baseUrl, "add/validRequestBodyNoOwner", token)
            add.response.status() shouldBe HttpStatusCode.Created

            val call = handleRequest(Get, "$baseUrl?id=4f982b2d-1b86-40de-8710-6bfb4649f1e3", bearerToken = token)

            call.response.status() shouldBe HttpStatusCode.OK
            call.response.content shouldBe fromFile(baseUrl, "get/SuccessResponseNoOwner")
        }
    }

    @Nested
    @DisplayName("add")
    inner class Add : Testing() {
        @Test
        fun `add new category`() = testApp(config) { token ->
            val call = handleRequest(Post, Paths.BillMan.categories, "add/validRequestBodyNoOwner", token)
            call.response.status() shouldBe HttpStatusCode.Created
            call.response.content shouldBe fromFile(baseUrl, "add/successResponse")
        }

        @Test
        fun `add invalid category`() = testApp(config) { token ->
            val call = handleRequest(Post, baseUrl, "add/invalidRequestBody", token)
            call.response.status() shouldBe HttpStatusCode.BadRequest
            call.response.content shouldBe fromFile(baseUrl, "add/invalidResponse")
        }
    }

    @Nested
    @DisplayName("update")
    inner class Update : Testing() {
        @Test
        fun `update existing category`() = testApp(config) { token ->
            val addCat = handleRequest(Post, Paths.BillMan.categories, "add/validRequestBody", token)
            addCat.response.status() shouldBe HttpStatusCode.Created

            val call = handleRequest(HttpMethod.Put, baseUrl, "update/validRequestBody", token)
            call.response.status() shouldBe HttpStatusCode.OK
            call.response.content
                ?.replace(Regex("\"dateUpdated\": \\d+"), "") shouldBe fromFile(baseUrl, "update/successResponse")
                .replace(Regex("\"dateUpdated\": \\d+"), "")
        }

        @Test
        fun `update existing category no owner`() = testApp(config) { token ->
            val addCat = handleRequest(Post, Paths.BillMan.categories, "add/validRequestBodyNoOwner", token)
            addCat.response.status() shouldBe HttpStatusCode.Created

            val call = handleRequest(HttpMethod.Put, baseUrl, "update/validRequestBody", token)
            call.response.status() shouldBe HttpStatusCode.BadRequest
            call.response.content shouldBe fromFile(baseUrl, "update/invalidResponseNoOwner")
        }

        @Test
        fun `update existing category not owned by user`() = testApp(config) { token ->
            val addCat = handleRequest(Post, Paths.BillMan.categories, "add/validRequestBodyNoOwner", token)
            addCat.response.status() shouldBe HttpStatusCode.Created

            val call = handleRequest(HttpMethod.Put, baseUrl, "update/validRequestBodyNotYours", token)
            call.response.status() shouldBe HttpStatusCode.BadRequest
            call.response.content shouldBe fromFile(baseUrl, "update/invalidResponseNotYours")
        }

        @Test
        fun `update non-existing category`() = testApp(config) { token ->
            val call = handleRequest(HttpMethod.Put, baseUrl, "update/nonExistRequestBody", token)
            call.response.status() shouldBe HttpStatusCode.BadRequest
            call.response.content shouldBe fromFile(baseUrl, "update/nonExistResponse")
        }

        @Test
        fun `update invalid category`() = testApp(config) { token ->
            val call = handleRequest(HttpMethod.Put, baseUrl, "update/invalidRequestBody", token)
            call.response.status() shouldBe HttpStatusCode.BadRequest
            call.response.content shouldBe fromFile(baseUrl, "update/invalidResponse")
        }
    }

    @Nested
    @DisplayName("delete")
    inner class Delete : Testing() {
        @Test
        fun `delete existing category`() = testApp(config) { token ->
            val addCat = handleRequest(Post, Paths.BillMan.categories, "add/validRequestBody", token)
            addCat.response.status() shouldBe HttpStatusCode.Created

            val call = handleRequest(
                method = HttpMethod.Delete,
                uri = "$baseUrl?id=4f982b2d-1b86-40de-8710-6bfb4649f1e3",
                bearerToken = token
            )
            call.response.content shouldBe fromFile(baseUrl, "delete/successResponse")
        }

        @Test
        fun `delete non-existing category`() = testApp(config) { token ->
            val call = handleRequest(
                method = HttpMethod.Delete,
                uri = "$baseUrl?id=4f493b2d-1b86-40de-8710-6bfb5032f1e2",
                bearerToken = token
            )
            call.response.status() shouldBe HttpStatusCode.NotFound
            call.response.content shouldBe fromFile(baseUrl, "delete/nonExistResponse")
        }

        @Test
        fun `delete invalid category`() = testApp(config) { token ->
            val call = handleRequest(HttpMethod.Delete, "$baseUrl?id=123", bearerToken = token)
            call.response.status() shouldBe HttpStatusCode.BadRequest
            call.response.content shouldBe fromFile(baseUrl, "delete/invalidResponse")
        }
    }
}