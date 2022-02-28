package com.weesnerdevelopment.billMan

import com.weesnerdevelopment.shared.Paths
import com.weesnerdevelopment.test.utils.fromFile
import com.weesnerdevelopment.test.utils.handleRequest
import com.weesnerdevelopment.test.utils.shouldBe
import com.weesnerdevelopment.test.utils.testApp
import io.ktor.http.*
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpMethod.Companion.Put
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TestIncomeRoutes : BillManTests() {
    override val baseUrl = Paths.BillMan.income

    @Nested
    @DisplayName("get all")
    inner class GetAll : Testing() {
        @Test
        fun `get all with no incomes returns empty`() = testApp(config) { token ->
            val call = handleRequest(Get, baseUrl, bearerToken = token)

            call.response.status() shouldBe HttpStatusCode.OK
            call.response.content shouldBe fromFile(baseUrl, "get/emptyResponse")
        }

        @Test
        fun `get all with 1 income returns incomes list`() = testApp(config) { token ->
            val add = handleRequest(Post, baseUrl, "add/validRequestBody", token)
            add.response.status() shouldBe HttpStatusCode.Created

            val call = handleRequest(Get, baseUrl, bearerToken = token)

            call.response.status() shouldBe HttpStatusCode.OK
            call.response.content shouldBe fromFile(baseUrl, "get/singleItemResponse")
        }
    }

    @Nested
    @DisplayName("get single")
    inner class GetSingle : Testing() {
        @Test
        fun `get single income that is not in the database`() = testApp(config) { token ->
            val call = handleRequest(Get, "$baseUrl?id=4f493b2d-1b86-40de-8710-6bfb5032f1e2", bearerToken = token)

            call.response.content shouldBe fromFile(baseUrl, "get/idNotFoundResponse")
        }

        @Test
        fun `get single income with invalid id`() = testApp(config) { token ->
            val call = handleRequest(Get, "$baseUrl?id=a", bearerToken = token)

            call.response.content shouldBe fromFile(baseUrl, "get/idInvalidResponse")
        }

        @Test
        fun `get single income that is in the database`() = testApp(config) { token ->
            val add = handleRequest(Post, baseUrl, "add/validRequestBody", token)
            add.response.status() shouldBe HttpStatusCode.Created

            val call = handleRequest(Get, "$baseUrl?id=4f982b2d-1b86-40de-8710-6bfb4649f1e4", bearerToken = token)

            call.response.content shouldBe fromFile(baseUrl, "get/successResponse")
        }
    }

    @Nested
    @DisplayName("add")
    inner class Add : Testing() {
        @Test
        fun `add new income`() = testApp(config) { token ->
            val call = handleRequest(Post, baseUrl, "add/validRequestBody", token)
            call.response.status() shouldBe HttpStatusCode.Created
            call.response.content shouldBe fromFile(baseUrl, "add/successResponse")
        }

        @Test
        fun `add invalid income`() = testApp(config) { token ->
            val call = handleRequest(Post, baseUrl, "add/invalidRequestBody", token)
            call.response.status() shouldBe HttpStatusCode.BadRequest
            call.response.content shouldBe fromFile(baseUrl, "add/invalidResponse")
        }
    }

    @Nested
    @DisplayName("update")
    inner class Update : Testing() {
        @Test
        fun `update existing income`() = testApp(config) { token ->
            val add = handleRequest(Post, baseUrl, "add/validRequestBody", token)
            add.response.status() shouldBe HttpStatusCode.Created

            val call = handleRequest(Put, baseUrl, "update/validRequestBody", token)
            call.response.status() shouldBe HttpStatusCode.OK
            call.response.content
                ?.replace(Regex("\"dateUpdated\": \\d+"), "") shouldBe fromFile(baseUrl, "update/successResponse")
                .replace(Regex("\"dateUpdated\": \\d+"), "")
        }

        @Test
        fun `update non-existing income`() = testApp(config) { token ->
            val call = handleRequest(Put, baseUrl, "update/validRequestBody", token)
            call.response.status() shouldBe HttpStatusCode.BadRequest
            call.response.content shouldBe fromFile(baseUrl, "update/nonExistResponse")
        }

        @Test
        fun `update invalid income`() = testApp(config) { token ->
            val call = handleRequest(Put, baseUrl, "update/invalidRequestBody", token)
            call.response.status() shouldBe HttpStatusCode.BadRequest
            call.response.content shouldBe fromFile(baseUrl, "update/invalidResponse")
        }
    }

    @Nested
    @DisplayName("delete")
    inner class Delete : Testing() {
        @Test
        fun `delete existing income`() = testApp(config) { token ->
            val add = handleRequest(Post, baseUrl, "add/validRequestBody", token)
            add.response.status() shouldBe HttpStatusCode.Created

            val call = handleRequest(Delete, "$baseUrl?id=4f982b2d-1b86-40de-8710-6bfb4649f1e4", bearerToken = token)
            call.response.content shouldBe fromFile(baseUrl, "delete/successResponse")
        }

        @Test
        fun `delete non-existing income`() = testApp(config) { token ->
            val call = handleRequest(Delete, "$baseUrl?id=4f493b2d-1b86-40de-8710-6bfb5032f1e2", bearerToken = token)
            call.response.status() shouldBe HttpStatusCode.NotFound
            call.response.content shouldBe fromFile(baseUrl, "delete/nonExistResponse")
        }

        @Test
        fun `delete invalid income`() = testApp(config) { token ->
            val call = handleRequest(Delete, "$baseUrl?id=123", bearerToken = token)
            call.response.status() shouldBe HttpStatusCode.BadRequest
            call.response.content shouldBe fromFile(baseUrl, "delete/invalidResponse")
        }
    }
}