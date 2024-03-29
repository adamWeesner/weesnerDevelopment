package taxFetcher

import Path
import com.weesnerdevelopment.shared.auth.User
import com.weesnerdevelopment.shared.base.History
import com.weesnerdevelopment.shared.taxFetcher.SocialSecurity
import com.weesnerdevelopment.shared.taxFetcher.responses.SocialSecurityResponse
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

class SocialSecurityTests : BaseTest("application-test.conf") {
    fun newItem(year: Int) = SocialSecurity(
        year = year,
        percent = 1.45,
        limit = 127200
    )

    val path = Path.TaxFetcher.socialSecurity

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
            val responseItems = response.content.parseResponse<SocialSecurityResponse>()?.items
            val item1 = responseItems!![responseItems.lastIndex - 1]
            val item2 = responseItems[responseItems.lastIndex]
            response.status() shouldBe HttpStatusCode.OK
            item1 shouldBe SocialSecurity(item1.id, 2000, 1.45, 127200, null, item1.dateCreated, item1.dateUpdated)
            item2 shouldBe SocialSecurity(item2.id, 2001, 1.45, 127200, null, item2.dateCreated, item2.dateUpdated)
        }
    }

    @Test
    @Order(3)
    fun `verify getting an added item`() {
        val item = BuiltRequest(engine, Post, path, token).asObject(newItem(2002))
        with(BuiltRequest(engine, Get, "$path/${item.year}", token).send<Unit>()) {
            val addedItem = response.content.parseResponse<SocialSecurity>()
            response.status() shouldBe HttpStatusCode.OK
            addedItem shouldBe SocialSecurity(
                item.id,
                2002,
                1.45,
                127200,
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
            val addedItem = response.content.parseResponse<SocialSecurity>()
            response.status() shouldBe HttpStatusCode.Created
            addedItem shouldBe SocialSecurity(
                addedItem?.id,
                2003,
                1.45,
                127200,
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
        val socialSecurity = BuiltRequest(engine, Post, path, token).asObject(newItem(2004))
        val updatedRequest =
            BuiltRequest(engine, Put, path, token).send(socialSecurity.copy(percent = 1.4, limit = 128000))

        with(updatedRequest) {
            val addedItem = response.content.parseResponse<SocialSecurity>()
            response.status() shouldBe HttpStatusCode.OK
            addedItem shouldBe SocialSecurity(
                addedItem?.id,
                2004,
                1.4,
                128000,
                listOf(
                    History(
                        addedItem?.history!![0].id,
                        "${addedItem::class.java.simpleName} ${addedItem.id} limit",
                        "127200.0",
                        "128000.0",
                        userAccount,
                        addedItem.history!![0].dateCreated,
                        addedItem.history!![0].dateUpdated
                    ),
                    History(
                        addedItem.history!![1].id,
                        "${addedItem::class.java.simpleName} ${addedItem.id} percent",
                        "1.45",
                        "1.4",
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
