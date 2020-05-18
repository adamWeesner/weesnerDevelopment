package taxFetcher

import BaseTest
import BuiltRequest
import com.weesnerdevelopment.utils.Path
import federalIncomeTax.FederalIncomeTaxResponse
import io.kotlintest.shouldBe
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpMethod.Companion.Put
import io.ktor.http.HttpStatusCode
import shared.fromJson
import shared.taxFetcher.FederalIncomeTax
import shared.taxFetcher.MaritalStatus.Single
import shared.taxFetcher.PayPeriod.Weekly

class FederalIncomeTaxTests : BaseTest({ token ->
    fun newItem(year: Int) = FederalIncomeTax(
        year = year,
        percent = 10.0,
        payPeriod = Weekly,
        maritalStatus = Single,
        nonTaxable = 0.0,
        plus = 0.0,
        notOver = 5.0,
        over = 1.0
    )

    val path = Path.TaxFetcher.federalIncomeTax

    "verify getting base url returns ok" {
        BuiltRequest(engine, Get, path, token).sendStatus<Unit>() shouldBe HttpStatusCode.OK
    }

    "verify getting base url returns all items in table" {
        BuiltRequest(engine, Post, path, token).send(newItem(2000))
        BuiltRequest(engine, Post, path, token).send(newItem(2001))
        with(BuiltRequest(engine, Get, path, token).send<Unit>()) {
            val responseItems = response.content?.fromJson<FederalIncomeTaxResponse>()?.items
            val item1 = responseItems!![responseItems.lastIndex - 1]
            val item2 = responseItems[responseItems.lastIndex]
            response.status() shouldBe HttpStatusCode.OK
            item1.year shouldBe 2000
            item2.year shouldBe 2001
        }
    }

    "verify getting an added item" {
        val item = BuiltRequest(engine, Post, path, token).asObject(newItem(2002))
        with(BuiltRequest(engine, Get, "$path/${item?.year?.toString()}", token).send<Unit>()) {
            val addedItem = response.content?.fromJson<FederalIncomeTax>()
            response.status() shouldBe HttpStatusCode.OK
            addedItem shouldBe FederalIncomeTax(
                item?.id,
                2002,
                Single,
                Weekly,
                1.0,
                5.0,
                0.0,
                10.0,
                0.0,
                null,
                addedItem?.dateCreated ?: 0,
                addedItem?.dateUpdated ?: 0
            )
        }
    }

    "verify getting an item that does not exist" {
        BuiltRequest(engine, Get, "$path/99", token).sendStatus<Unit>() shouldBe HttpStatusCode.NotFound
    }

    "verify adding a new item" {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(2003)) shouldBe HttpStatusCode.Created
    }

    "verify adding a duplicate item" {
        BuiltRequest(engine, Post, path, token).send(newItem(2008))
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(2008)) shouldBe HttpStatusCode.Conflict
    }

    "verify updating an added item" {
        val federalIncomeTax = BuiltRequest(engine, Post, path, token).asObject(newItem(2004))
        val updateRequest =
            BuiltRequest(engine, Put, path, token).send(federalIncomeTax?.copy(percent = 1.4, over = 2.5))

        with(updateRequest) {
            val addedItem = response.content?.fromJson<FederalIncomeTax>()
            response.status() shouldBe HttpStatusCode.OK
            addedItem shouldBe FederalIncomeTax(
                addedItem?.id,
                2004,
                Single,
                Weekly,
                2.5,
                5.0,
                0.0,
                1.4,
                0.0,
                addedItem?.history,
                addedItem?.dateCreated ?: 0,
                addedItem?.dateUpdated ?: 0
            )
            addedItem?.history?.get(0)?.field shouldBe "${addedItem!!::class.java.simpleName} ${addedItem.id} over"
            addedItem.history?.get(1)?.field shouldBe "${addedItem::class.java.simpleName} ${addedItem.id} percent"
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

    "verify adding new data where 'over' is between" {
        BuiltRequest(engine, Post, path, token)
            .sendStatus(newItem(2006).copy(over = 2.0)) shouldBe HttpStatusCode.Conflict
    }

    "verify adding new data where 'notOver' is between" {
        BuiltRequest(engine, Post, path, token)
            .sendStatus(newItem(2006).copy(notOver = 2.0)) shouldBe HttpStatusCode.Conflict
    }

    "verify adding new data where 'over' is between and 'notOver' is not" {
        BuiltRequest(engine, Post, path, token)
            .sendStatus(newItem(2006).copy(over = 2.0, notOver = 10.0)) shouldBe HttpStatusCode.Conflict
    }
})
