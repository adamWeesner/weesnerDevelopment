import PayPeriod.Biweekly
import PayPeriod.Weekly
import com.weesnerdevelopment.Paths
import com.weesnerdevelopment.fromJson
import com.weesnerdevelopment.main
import com.weesnerdevelopment.toJson
import io.kotlintest.shouldBe
import io.ktor.application.Application
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpMethod.Companion.Put
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.withTestApplication
import taxWithholding.TaxWithholding
import taxWithholding.TaxWithholdingResponse
import taxWithholding.TaxWithholdingTypes.General
import kotlin.test.Test

class TaxWithholdingTests : BaseTest() {
    private fun newItem(year: Int) = TaxWithholding(
        year = year,
        amount = 1.23,
        payPeriod = Weekly,
        type = General
    )

    @Test
    fun `verify getting base url returns ok`() = withTestApplication(Application::main) {
        request(Get, Paths.taxWithholding).response.status() shouldBe HttpStatusCode.OK
    }

    @Test
    fun `verify getting base url returns all items in table`() = withTestApplication(Application::main) {
        bodyRequest(Post, Paths.taxWithholding, newItem(2000).toJson())
        bodyRequest(Post, Paths.taxWithholding, newItem(2001).toJson())

        with(request(Get, Paths.taxWithholding)) {
            val responseItems = response.content?.fromJson<TaxWithholdingResponse>()?.taxWithholding
            val item1 = responseItems!![responseItems.lastIndex - 1]
            val item2 = responseItems[responseItems.lastIndex]

            response.status() shouldBe HttpStatusCode.OK

            item1 shouldBe TaxWithholding(item1.id, 2000, General, Weekly, 1.23, item1.dateCreated, item1.dateUpdated)
            item2 shouldBe TaxWithholding(item2.id, 2001, General, Weekly, 1.23, item2.dateCreated, item2.dateUpdated)
        }
    }

    @Test
    fun `verify getting an added item`() = withTestApplication(Application::main) {
        val id = requestToObject<TaxWithholding>(Post, Paths.taxWithholding, newItem(2002).toJson())?.id

        with(request(Get, Paths.taxWithholding, id?.toString())) {
            val addedItem = response.content!!.fromJson<TaxWithholding>()!!

            response.status() shouldBe HttpStatusCode.OK
            addedItem shouldBe TaxWithholding(
                id,
                2002,
                General,
                Weekly,
                1.23,
                addedItem.dateCreated,
                addedItem.dateUpdated
            )
        }
    }

    @Test
    fun `verify getting an item that does not exist`() = withTestApplication(Application::main) {
        request(Get, Paths.taxWithholding, "99").response.status() shouldBe HttpStatusCode.NotFound
    }

    @Test
    fun `verify adding a new item`() = withTestApplication(Application::main) {
        with(bodyRequest(Post, Paths.taxWithholding, newItem(2003).toJson())) {
            val addedItem = response.content!!.fromJson<TaxWithholding>()!!

            response.status() shouldBe HttpStatusCode.Created
            addedItem shouldBe TaxWithholding(
                addedItem.id,
                2003,
                General,
                Weekly,
                1.23,
                addedItem.dateCreated,
                addedItem.dateUpdated
            )
        }
    }

    @Test
    fun `verify updating an added item`() = withTestApplication(Application::main) {
        bodyRequest(Post, Paths.taxWithholding, newItem(2004).toJson())

        with(
            bodyRequest(
                Put,
                Paths.taxWithholding,
                newItem(2004).copy(id = 1, amount = 1.4, payPeriod = Biweekly).toJson()
            )
        ) {
            val addedItem = response.content!!.fromJson<TaxWithholding>()!!

            response.status() shouldBe HttpStatusCode.OK
            addedItem shouldBe TaxWithholding(
                1,
                2004,
                General,
                Biweekly,
                1.4,
                addedItem.dateCreated,
                addedItem.dateUpdated
            )
        }
    }

    @Test
    fun `verify updating a non existent item`() = withTestApplication(Application::main) {
        bodyRequest(
            Put,
            Paths.taxWithholding,
            newItem(2005).copy(99).toJson()
        ).response.status() shouldBe HttpStatusCode.NotFound
    }

    @Test
    fun `verify updating without an id adds a new item`() = withTestApplication(Application::main) {
        bodyRequest(Put, Paths.taxWithholding, newItem(2006).toJson()).response.status() shouldBe HttpStatusCode.Created
    }

    @Test
    fun `verify deleting and item that has been added`() = withTestApplication(Application::main) {
        bodyRequest(Post, Paths.taxWithholding, newItem(2007).toJson())
        request(Delete, Paths.taxWithholding, "1").response.status() shouldBe HttpStatusCode.OK
    }

    @Test
    fun `verify deleting item that doesn't exist`() = withTestApplication(Application::main) {
        request(Delete, Paths.taxWithholding, "99").response.status() shouldBe HttpStatusCode.NotFound
    }
}