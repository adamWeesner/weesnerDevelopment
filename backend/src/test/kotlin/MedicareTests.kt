import com.weesnerdevelopment.Paths
import com.weesnerdevelopment.fromJson
import com.weesnerdevelopment.main
import com.weesnerdevelopment.toJson
import io.kotlintest.shouldBe
import io.ktor.application.Application
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import medicare.Medicare
import medicare.MedicareLimit
import medicare.MedicareResponse
import kotlin.test.Test
import kotlin.test.assertEquals


class MedicareTests : BaseTest() {
    private val newItem = Medicare(
        year = 2017,
        percent = 6.25,
        additionalPercent = 0.9,
        limits = listOf(
            MedicareLimit(
                year = 2017,
                amount = 200000,
                maritalStatus = MaritalStatus.Single
            )
        )
    )

    private fun newItem(year: Int) = Medicare(
        year = year,
        percent = 6.25,
        additionalPercent = 0.9,
        limits = listOf(
            MedicareLimit(
                year = year,
                amount = 200000,
                maritalStatus = MaritalStatus.Single
            )
        )
    )

    @Test
    fun `verify getting base url returns ok`() = withTestApplication(Application::main) {
        with(handleRequest(HttpMethod.Get, "/${Paths.medicare.name}")) {
            assertEquals(HttpStatusCode.OK, response.status())
        }
    }

    @Test
    fun `verify getting base url returns all items in table`() = withTestApplication(Application::main) {
        addNewItem(Paths.medicare, newItem(2018).toJson())
        addNewItem(Paths.medicare, newItem(2019).toJson())

        with(handleRequest(HttpMethod.Get, "/${Paths.medicare.name}")) {
            val responseItems = response.content?.fromJson<MedicareResponse>()?.medicare
            println(responseItems?.size)
            val item1 = responseItems!![responseItems.lastIndex - 1]
            val item2 = responseItems[responseItems.lastIndex]
            assertEquals(HttpStatusCode.OK, response.status())
            item1 shouldBe Medicare(item1.id, 2018, 6.25, 0.9, item1.limits, item1.dateCreated, item1.dateUpdated)
            item2 shouldBe Medicare(item2.id, 2019, 6.25, 0.9, item2.limits, item2.dateCreated, item2.dateUpdated)
        }
    }

    @Test
    fun `verify getting an added item`() = withTestApplication(Application::main) {
        addNewItem(Paths.medicare, newItem.toJson())

        with(handleRequest(HttpMethod.Get, "/${Paths.medicare.name}/1")) {
            val addedItem = response.content!!.fromJson<Medicare>()!!

            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals(
                Medicare(1, 2017, 6.25, 0.9, addedItem.limits, addedItem.dateCreated, addedItem.dateUpdated),
                addedItem
            )
        }
    }

    @Test
    fun `verify getting an item that does not exist`() = withTestApplication(Application::main) {
        with(handleRequest(HttpMethod.Get, "/${Paths.medicare.name}/99")) {
            assertEquals(HttpStatusCode.NotFound, response.status())
        }
    }

    @Test
    fun `verify adding a new item`() = withTestApplication(Application::main) {
        with(addNewItem(Paths.medicare, newItem.toJson())) {
            val addedItem = response.content!!.fromJson<Medicare>()!!

            assertEquals(HttpStatusCode.Created, response.status())
            assertEquals(
                Medicare(
                    addedItem.id,
                    2017,
                    6.25,
                    0.9,
                    listOf(
                        MedicareLimit(
                            addedItem.limits[0].id,
                            2017,
                            MaritalStatus.Single,
                            200000,
                            addedItem.limits[0].dateCreated,
                            addedItem.limits[0].dateUpdated
                        )
                    ),
                    addedItem.dateCreated,
                    addedItem.dateUpdated
                ),
                addedItem
            )
        }
    }

    @Test
    fun `verify updating an added item`() = withTestApplication(Application::main) {
        addNewItem(Paths.medicare, newItem.toJson())

        with(handleRequest(HttpMethod.Put, "/${Paths.medicare.name}") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(newItem.copy(id = 1, percent = 6.0).toJson())
        }) {
            val addedItem = response.content!!.fromJson<Medicare>()!!

            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals(
                Medicare(
                    1,
                    2017,
                    6.0,
                    addedItem.additionalPercent,
                    addedItem.limits,
                    addedItem.dateCreated,
                    addedItem.dateUpdated
                ),
                addedItem
            )
        }
    }

    @Test
    fun `verify updating a non existent item`() = withTestApplication(Application::main) {
        with(handleRequest(HttpMethod.Put, "/${Paths.medicare.name}") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(newItem.copy(99).toJson())
        }) {
            assertEquals(HttpStatusCode.NotFound, response.status())
        }
    }

    @Test
    fun `verify updating without an id adds a new item`() = withTestApplication(Application::main) {
        with(handleRequest(HttpMethod.Put, "/${Paths.medicare.name}") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(newItem(2020).toJson())
        }) {
            assertEquals(HttpStatusCode.Created, response.status())
        }
    }

    @Test
    fun `verify deleting and item that has been added`() = withTestApplication(Application::main) {
        addNewItem(Paths.medicare, newItem(2016).toJson())

        var addedItemId: Int? = null

        with(handleRequest(HttpMethod.Get, "/${Paths.medicare.name}")) {
            val responseItems = response.content?.fromJson<MedicareResponse>()?.medicare
            println(responseItems?.filter { it.year == 2016 })
            addedItemId = responseItems?.find { it.year == 2016 }?.id
        }

        with(handleRequest(HttpMethod.Delete, "/${Paths.medicare.name}/$addedItemId") {
        }) {
            assertEquals(HttpStatusCode.OK, response.status())
        }
    }

    @Test
    fun `verify deleting item that doesnt exist`() = withTestApplication(Application::main) {
        with(handleRequest(HttpMethod.Delete, "/${Paths.medicare.name}/99") {
        }) {
            assertEquals(HttpStatusCode.NotFound, response.status())
        }
    }
}