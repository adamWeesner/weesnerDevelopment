package taxFetcher

import BaseTest
import BuiltRequest
import com.weesnerdevelopment.utils.Path
import generics.InvalidAttributeException
import io.kotlintest.shouldBe
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpMethod.Companion.Put
import io.ktor.http.HttpStatusCode
import medicare.MedicareResponse
import shared.fromJson
import shared.taxFetcher.MaritalStatus.Single
import shared.taxFetcher.Medicare
import shared.taxFetcher.MedicareLimit

class MedicareTests : BaseTest({ token ->
    fun newItem(year: Int) = Medicare(
        year = year,
        percent = 6.25,
        additionalPercent = 0.9,
        limits = listOf(
            MedicareLimit(
                year = year,
                amount = 200000,
                maritalStatus = Single
            )
        )
    )

    val path = Path.TaxFetcher.medicare

    "verify getting base url returns ok" {
        BuiltRequest(engine, Get, path, token).sendStatus<Unit>() shouldBe HttpStatusCode.OK
    }

    "verify getting base url returns all items in table" {
        BuiltRequest(engine, Post, path, token).send(newItem(2000))
        BuiltRequest(engine, Post, path, token).send(newItem(2001))
        with(BuiltRequest(engine, Get, path, token).send<Unit>()) {
            val responseItems = response.content?.fromJson<MedicareResponse>()?.items
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
            val addedItem = response.content?.fromJson<Medicare>()
            response.status() shouldBe HttpStatusCode.OK
            addedItem shouldBe Medicare(
                item?.id,
                2002,
                6.25,
                0.9,
                addedItem?.limits ?: listOf(),
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
        val medicare = BuiltRequest(engine, Post, path, token).asObject(newItem(2004))
        val newLimits = listOf(medicare?.limits?.run {
            first().copy(amount = 123456)
        } ?: throw InvalidAttributeException("Limits"))

        val updateRequest =
            BuiltRequest(engine, Put, path, token).send(medicare?.copy(percent = 6.0, limits = newLimits))

        with(updateRequest) {
            val addedItem = response.content?.fromJson<Medicare>()
            response.status() shouldBe HttpStatusCode.OK
            addedItem shouldBe Medicare(
                addedItem!!.id,
                2004,
                6.0,
                addedItem.additionalPercent,
                addedItem.limits,
                addedItem.history,
                addedItem.dateCreated,
                addedItem.dateUpdated
            )
            addedItem.history?.get(0)?.field shouldBe "${addedItem::class.java.simpleName} ${addedItem.id} percent"
            addedItem.history?.get(1)?.field shouldBe "${MedicareLimit::class.java.simpleName} ${addedItem.limits.first().id} amount"
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
        val addedItem =
            BuiltRequest(engine, Get, path, token).asObject<MedicareResponse>()?.items?.find { it.year == 2007 }?.year
        BuiltRequest(engine, Delete, "$path/${addedItem?.toString()}", token)
            .sendStatus<Unit>() shouldBe HttpStatusCode.OK
    }

    "verify deleting item that doesn't exist" {
        BuiltRequest(engine, Delete, "$path/2099", token).sendStatus<Unit>() shouldBe HttpStatusCode.NotFound
    }
})
