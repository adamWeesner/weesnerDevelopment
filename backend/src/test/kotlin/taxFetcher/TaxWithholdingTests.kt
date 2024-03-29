package taxFetcher

import Path
import com.weesnerdevelopment.shared.auth.User
import com.weesnerdevelopment.shared.base.History
import com.weesnerdevelopment.shared.taxFetcher.PayPeriod.Biweekly
import com.weesnerdevelopment.shared.taxFetcher.PayPeriod.Weekly
import com.weesnerdevelopment.shared.taxFetcher.TaxWithholding
import com.weesnerdevelopment.shared.taxFetcher.TaxWithholdingTypes.General
import com.weesnerdevelopment.shared.taxFetcher.responses.TaxWithholdingResponse
import com.weesnerdevelopment.test.utils.BaseTest
import com.weesnerdevelopment.test.utils.BuiltRequest
import com.weesnerdevelopment.test.utils.parseResponse
import com.weesnerdevelopment.test.utils.shouldBe
import io.ktor.http.*
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpMethod.Companion.Put
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test

class TaxWithholdingTests : BaseTest("application-test.conf") {
    fun newItem(year: Int) = TaxWithholding(
        year = year,
        amount = 1.23,
        payPeriod = Weekly,
        type = General
    )

    val path = Path.TaxFetcher.taxWithholding

    @Test
    @Order(1)
    fun `verify getting base url returns ok`() {
        BuiltRequest(engine, Get, path, token).sendStatus<Unit>() shouldBe HttpStatusCode.OK
    }

    @Test
    @Order(2)
    fun `verify getting base url returns all items in table`() {
        BuiltRequest(engine, Post, path, token).send(newItem(2000))
        BuiltRequest(engine, Post, path, token).send(newItem(2001))

        with(BuiltRequest(engine, Get, path, token).send<Unit>()) {
            val responseItems = response.content.parseResponse<TaxWithholdingResponse>()?.items
            val item1 = responseItems!![responseItems.lastIndex - 1]
            val item2 = responseItems[responseItems.lastIndex]

            response.status() shouldBe HttpStatusCode.OK

            item1 shouldBe TaxWithholding(
                item1.id,
                2000,
                General,
                Weekly,
                1.23,
                null,
                item1.dateCreated,
                item1.dateUpdated
            )
            item2 shouldBe TaxWithholding(
                item2.id,
                2001,
                General,
                Weekly,
                1.23,
                null,
                item2.dateCreated,
                item2.dateUpdated
            )
        }
    }

    @Test
    @Order(3)
    fun `verify getting an added item`() {
        val item = BuiltRequest(engine, Post, path, token).asObject(newItem(2002))

        with(BuiltRequest(engine, Get, "$path/${item.year}", token).send<Unit>()) {
            val addedItem = response.content.parseResponse<TaxWithholding>()

            response.status() shouldBe HttpStatusCode.OK
            addedItem shouldBe TaxWithholding(
                item.id,
                2002,
                General,
                Weekly,
                1.23,
                null,
                addedItem?.dateCreated ?: 0,
                addedItem?.dateUpdated ?: 0
            )
        }
    }

    @Test
    @Order(4)
    fun `verify adding a duplicate item`() {
        BuiltRequest(engine, Post, path, token).send(newItem(2008))
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(2008)) shouldBe HttpStatusCode.Conflict
    }

    @Test
    @Order(5)
    fun `verify getting an item that does not exist`() {
        BuiltRequest(engine, Get, "$path/99", token).sendStatus<Unit>() shouldBe HttpStatusCode.NotFound
    }

    @Test
    @Order(6)
    fun `verify adding a new item`() {
        with(BuiltRequest(engine, Post, path, token).send(newItem(2003))) {
            val addedItem = response.content.parseResponse<TaxWithholding>()

            response.status() shouldBe HttpStatusCode.Created
            addedItem shouldBe TaxWithholding(
                addedItem?.id,
                2003,
                General,
                Weekly,
                1.23,
                null,
                addedItem?.dateCreated ?: 0,
                addedItem?.dateUpdated ?: 0
            )
        }
    }

    @Test
    @Order(7)
    fun `verify updating an added item`() {
        val userAccount = BuiltRequest(engine, Get, "${Path.User.basePath}${Path.User.account}", token).asObject<User>()
        val taxWithholding = BuiltRequest(engine, Post, path, token).asObject(newItem(2004))
        val updateRequest =
            BuiltRequest(engine, Put, path, token).send(taxWithholding.copy(amount = 1.4, payPeriod = Biweekly))

        with(updateRequest) {
            val addedItem = response.content.parseResponse<TaxWithholding>()

            response.status() shouldBe HttpStatusCode.OK
            addedItem shouldBe TaxWithholding(
                addedItem?.id,
                2004,
                General,
                Biweekly,
                1.4,
                listOf(
                    History(
                        addedItem?.history!![0].id,
                        "${addedItem::class.java.simpleName} ${addedItem.id} amount",
                        "1.23",
                        "1.4",
                        userAccount,
                        addedItem.history!![0].dateCreated,
                        addedItem.history!![0].dateUpdated
                    ),
                    History(
                        addedItem.history!![1].id,
                        "${addedItem::class.java.simpleName} ${addedItem.id} payPeriod",
                        "Weekly",
                        "Biweekly",
                        userAccount,
                        addedItem.history!![1].dateCreated,
                        addedItem.history!![1].dateUpdated
                    )
                ),
                addedItem.dateCreated,
                addedItem.dateUpdated
            )
        }
    }

    @Test
    @Order(8)
    fun `verify updating a non existent item`() {
        BuiltRequest(engine, Put, path, token).sendStatus(newItem(2005).copy(99)) shouldBe HttpStatusCode.BadRequest
    }

    @Test
    @Order(9)
    fun `verify updating without an id adds a new item`() {
        BuiltRequest(engine, Put, path, token).sendStatus(newItem(2006)) shouldBe HttpStatusCode.Created
    }

    @Test
    @Order(10)
    fun `verify deleting and item that has been added`() {
        BuiltRequest(engine, Post, path, token).send(newItem(2007))
        BuiltRequest(engine, Delete, "$path/2007", token).sendStatus<Unit>() shouldBe HttpStatusCode.OK
    }

    @Test
    @Order(11)
    fun `verify deleting item that doesn't exist`() {
        BuiltRequest(engine, Delete, "$path/2099", token).sendStatus<Unit>() shouldBe HttpStatusCode.NotFound
    }
}
