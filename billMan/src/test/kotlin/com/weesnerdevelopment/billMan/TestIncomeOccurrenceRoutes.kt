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
import io.ktor.server.testing.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TestIncomeOccurrenceRoutes : BillManTests() {
    override val baseUrl = Paths.BillMan.incomeOccurrences

    private fun TestApplicationEngine.addIncome(token: String) {
        val addIncome = handleRequest(Post, Paths.BillMan.income, "add/validRequestBody", token)
        addIncome.response.status() shouldBe HttpStatusCode.Created
    }

    @Nested
    @DisplayName("get all")
    inner class GetAll : Testing() {
        @Test
        fun `get all with no bill occurrences returns empty`() = testApp(config) { token ->
            val call = handleRequest(Get, baseUrl, bearerToken = token)

            call.response.status() shouldBe HttpStatusCode.OK
            call.response.content shouldBe fromFile(baseUrl, "get/emptyResponse")
        }

        @Test
        fun `get all of 1 bills occurrences returns occurrences list`() = testApp(config) { token ->
            addIncome(token)

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
        fun `get single occurrence that is not in the database`() = testApp(config) { token ->
            val call = handleRequest(Get, "$baseUrl?id=4f493b2d-1b86-40de-8710-6bfb5032f1e2", bearerToken = token)

            call.response.content shouldBe fromFile(baseUrl, "get/idNotFoundResponse")
        }

        @Test
        fun `get single occurrence with invalid id`() = testApp(config) { token ->
            val call = handleRequest(Get, "$baseUrl?id=a", bearerToken = token)

            call.response.content shouldBe fromFile(baseUrl, "get/idInvalidResponse")
        }

        @Test
        fun `get single occurrence that is in the database`() = testApp(config) { token ->
            addIncome(token)

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
        fun `add new occurrence`() = testApp(config) { token ->
            addIncome(token)

            val call = handleRequest(Post, baseUrl, "add/validRequestBody", token)
            call.response.status() shouldBe HttpStatusCode.Created
            call.response.content shouldBe fromFile(baseUrl, "add/successResponse")
        }

        @Test
        fun `add invalid occurrence`() = testApp(config) { token ->
            val call = handleRequest(Post, baseUrl, "add/invalidRequestBody", token)
            call.response.status() shouldBe HttpStatusCode.BadRequest
            call.response.content shouldBe fromFile(baseUrl, "add/invalidResponse")
        }
    }

    @Nested
    @DisplayName("update")
    inner class Update : Testing() {
        @Test
        fun `update existing occurrence`() = testApp(config) { token ->
            addIncome(token)

            val add = handleRequest(Post, baseUrl, "add/validRequestBody", token)
            add.response.status() shouldBe HttpStatusCode.Created

            val call = handleRequest(Put, baseUrl, "update/validRequestBody", token)
            call.response.status() shouldBe HttpStatusCode.OK
            call.response.content
                ?.replace(Regex("\"dateUpdated\": \\d+"), "") shouldBe fromFile(baseUrl, "update/successResponse")
                .replace(Regex("\"dateUpdated\": \\d+"), "")
        }

        @Test
        fun `update non-existing occurrence`() = testApp(config) { token ->
            val call = handleRequest(Put, baseUrl, "update/validRequestBody", token)
            call.response.status() shouldBe HttpStatusCode.BadRequest
            call.response.content shouldBe fromFile(baseUrl, "update/nonExistResponse")
        }

        @Test
        fun `update invalid occurrence`() = testApp(config) { token ->
            val call = handleRequest(Put, baseUrl, "update/invalidRequestBody", token)
            call.response.status() shouldBe HttpStatusCode.BadRequest
            call.response.content shouldBe fromFile(baseUrl, "update/invalidResponse")
        }
    }

    @Nested
    @DisplayName("delete")
    inner class Delete : Testing() {
        @Test
        fun `delete existing occurrence`() = testApp(config) { token ->
            addIncome(token)

            val add = handleRequest(Post, baseUrl, "add/validRequestBody", token)
            add.response.status() shouldBe HttpStatusCode.Created

            val call = handleRequest(Delete, "$baseUrl?id=4f982b2d-1b86-40de-8710-6bfb4649f1e4", bearerToken = token)
            call.response.content shouldBe fromFile(baseUrl, "delete/successResponse")
        }

        @Test
        fun `delete non-existing occurrence`() = testApp(config) { token ->
            val call = handleRequest(Delete, "$baseUrl?id=4f493b2d-1b86-40de-8710-6bfb5032f1e2", bearerToken = token)
            call.response.status() shouldBe HttpStatusCode.NotFound
            call.response.content shouldBe fromFile(baseUrl, "delete/nonExistResponse")
        }

        @Test
        fun `delete invalid occurrence`() = testApp(config) { token ->
            val call = handleRequest(Delete, "$baseUrl?id=123", bearerToken = token)
            call.response.status() shouldBe HttpStatusCode.BadRequest
            call.response.content shouldBe fromFile(baseUrl, "delete/invalidResponse")
        }
    }
}