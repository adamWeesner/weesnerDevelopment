package taxFetcher

import BaseTest
import BuiltRequest
import com.weesnerdevelopment.utils.Path
import io.kotlintest.shouldBe
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpMethod.Companion.Put
import io.ktor.http.HttpStatusCode
import parse
import shared.auth.User
import shared.base.History
import shared.fromJson
import shared.taxFetcher.PayPeriod.Biweekly
import shared.taxFetcher.PayPeriod.Weekly
import shared.taxFetcher.TaxWithholding
import shared.taxFetcher.TaxWithholdingTypes.General
import taxWithholding.TaxWithholdingResponse

class TaxWithholdingTests : BaseTest({ token ->
    fun newItem(year: Int) = TaxWithholding(
        year = year,
        amount = 1.23,
        payPeriod = Weekly,
        type = General
    )

    val path = Path.TaxFetcher.taxWithholding

    "verify getting base url returns ok" {
        BuiltRequest(engine, Get, path, token).sendStatus<Unit>() shouldBe HttpStatusCode.OK
    }

    "verify getting base url returns all items in table" {
        BuiltRequest(engine, Post, path, token).send(newItem(2000))
        BuiltRequest(engine, Post, path, token).send(newItem(2001))

        with(BuiltRequest(engine, Get, path, token).send<Unit>()) {
            val responseItems = response.content?.fromJson<TaxWithholdingResponse>()?.items
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

    "verify getting an added item" {
        val item = BuiltRequest(engine, Post, path, token).asObject(newItem(2002))

        with(BuiltRequest(engine, Get, "$path/${item.year}", token).send<Unit>()) {
            val addedItem = response.content.parse<TaxWithholding>()

            response.status() shouldBe HttpStatusCode.OK
            addedItem shouldBe TaxWithholding(
                item.id,
                2002,
                General,
                Weekly,
                1.23,
                null,
                addedItem.dateCreated,
                addedItem.dateUpdated
            )
        }
    }

    "verify adding a duplicate item" {
        BuiltRequest(engine, Post, path, token).send(newItem(2008))
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(2008)) shouldBe HttpStatusCode.Conflict
    }

    "verify getting an item that does not exist" {
        BuiltRequest(engine, Get, "$path/99", token).sendStatus<Unit>() shouldBe HttpStatusCode.NotFound
    }

    "verify adding a new item" {
        with(BuiltRequest(engine, Post, path, token).send(newItem(2003))) {
            val addedItem = response.content.parse<TaxWithholding>()

            response.status() shouldBe HttpStatusCode.Created
            addedItem shouldBe TaxWithholding(
                addedItem.id,
                2003,
                General,
                Weekly,
                1.23,
                null,
                addedItem.dateCreated,
                addedItem.dateUpdated
            )
        }
    }

    "verify updating an added item" {
        val userAccount = BuiltRequest(engine, Get, "${Path.User.base}${Path.User.account}", token).asObject<User>()
        val taxWithholding = BuiltRequest(engine, Post, path, token).asObject(newItem(2004))
        val updateRequest =
            BuiltRequest(engine, Put, path, token).send(taxWithholding.copy(amount = 1.4, payPeriod = Biweekly))

        with(updateRequest) {
            val addedItem = response.content.parse<TaxWithholding>()

            response.status() shouldBe HttpStatusCode.OK
            addedItem shouldBe TaxWithholding(
                addedItem.id,
                2004,
                General,
                Biweekly,
                1.4,
                listOf(
                    History(
                        addedItem.history!![0].id,
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

    "verify updating a non existent item" {
        BuiltRequest(engine, Put, path, token).sendStatus(newItem(2005).copy(99)) shouldBe HttpStatusCode.BadRequest
    }

    "verify updating without an id adds a new item" {
        BuiltRequest(engine, Put, path, token).sendStatus(newItem(2006)) shouldBe HttpStatusCode.Created
    }

    "verify deleting and item that has been added" {
        BuiltRequest(engine, Post, path, token).send(newItem(2007))
        BuiltRequest(engine, Delete, "$path/2007", token).sendStatus<Unit>() shouldBe HttpStatusCode.OK
    }

    "verify deleting item that doesn't exist" {
        BuiltRequest(engine, Delete, "$path/2099", token).sendStatus<Unit>() shouldBe HttpStatusCode.NotFound
    }
})
