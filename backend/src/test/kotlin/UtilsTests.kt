import com.weesnerdevelopment.shared.auth.User
import com.weesnerdevelopment.shared.base.GenericItem
import com.weesnerdevelopment.shared.base.History
import com.weesnerdevelopment.shared.billMan.Bill
import com.weesnerdevelopment.shared.billMan.Category
import com.weesnerdevelopment.shared.billMan.Color
import com.weesnerdevelopment.shared.taxFetcher.MaritalStatus
import com.weesnerdevelopment.shared.taxFetcher.Medicare
import com.weesnerdevelopment.shared.taxFetcher.MedicareLimit
import com.weesnerdevelopment.shared.taxFetcher.SocialSecurity
import com.weesnerdevelopment.shared.toJson
import com.weesnerdevelopment.test.utils.shouldBe
import org.junit.jupiter.api.Test

open class UtilsTests {
    val fakeUser =
        User(id = 1, uuid = "randomuuid", name = "test", email = "test@email.com", dateUpdated = 1, dateCreated = 1)

    data class Random(
        override val id: Int? = null,
        val name: String,
        val value: String = "1",
        override val dateCreated: Long = 1,
        override val dateUpdated: Long = 1
    ) : GenericItem

    @Test
    fun `verify diffing Social Security item`() {
        val firstItem = SocialSecurity(1, 2020, 3.4, 128000, null, 1, 1)
        val secondItem = SocialSecurity(1, 2020, 3.6, 127500, null, 1, 1)

        firstItem.diff(secondItem).updates(fakeUser).apply {
            this shouldBe listOf(
                History(
                    null,
                    "SocialSecurity 1 limit",
                    "128000",
                    "127500",
                    fakeUser,
                    this[0].dateCreated,
                    this[0].dateUpdated
                ),
                History(
                    null,
                    "SocialSecurity 1 percent",
                    "3.4",
                    "3.6",
                    fakeUser,
                    this[1].dateCreated,
                    this[1].dateUpdated
                )
            )
        }
    }

    @Test
    fun `verify diffing Social Security item with history`() {
        val firstItem = SocialSecurity(1, 2020, 3.4, 128000, listOf(History(1, "", "", "", fakeUser)), 1, 1)
        val secondItem = SocialSecurity(1, 2020, 3.6, 127500, null, 1, 1)

        firstItem.diff(secondItem).updates(fakeUser).apply {
            this shouldBe listOf(
                History(
                    null,
                    "SocialSecurity 1 limit",
                    "128000",
                    "127500",
                    fakeUser,
                    this[0].dateCreated,
                    this[0].dateUpdated
                ),
                History(
                    null,
                    "SocialSecurity 1 percent",
                    "3.4",
                    "3.6",
                    fakeUser,
                    this[1].dateCreated,
                    this[1].dateUpdated
                )
            )
        }
    }

    @Test
    fun `verify diffing Medicare item`() {
        val firstItem = Medicare(
            1,
            2020,
            3.4,
            1.1,
            listOf(
                MedicareLimit(1, 2020, MaritalStatus.Single, 123),
                MedicareLimit(2, 2020, MaritalStatus.Married, 234),
                MedicareLimit(3, 2020, MaritalStatus.Separate, 345)
            )
        )
        val secondItem = Medicare(
            1,
            2020,
            3.4,
            1.2,
            listOf(
                MedicareLimit(1, 2020, MaritalStatus.Single, 124),
                MedicareLimit(2, 2020, MaritalStatus.Married, 234),
                MedicareLimit(3, 2020, MaritalStatus.Separate, 346)
            )
        )

        firstItem.diff(secondItem).updates(fakeUser).apply {
            this shouldBe listOf(
                History(
                    null,
                    "Medicare 1 additionalPercent",
                    "1.1",
                    "1.2",
                    fakeUser,
                    this[0].dateCreated,
                    this[0].dateUpdated
                ),
                History(
                    null,
                    "MedicareLimit 1 amount",
                    "123",
                    "124",
                    fakeUser,
                    this[1].dateCreated,
                    this[1].dateUpdated
                ),
                History(
                    null,
                    "MedicareLimit 3 amount",
                    "345",
                    "346",
                    fakeUser,
                    this[2].dateCreated,
                    this[2].dateUpdated
                )
            )
        }
    }

    @Test
    fun `verify diffing Bill item`() {
        val firstItem = Bill(
            id = 1,
            owner = fakeUser,
            name = "bill1",
            amount = "1.23",
            varyingAmount = false,
            payoffAmount = null,
            sharedUsers = null,
            categories = listOf(
                Category(
                    id = 1,
                    owner = fakeUser,
                    name = "cat1",
                    dateUpdated = 1,
                    dateCreated = 1
                )
            ),
            color = Color(id = 1, red = 255, green = 255, blue = 255, alpha = 255, dateUpdated = 1, dateCreated = 1)
        )

        val secondItem = Bill(
            id = 1,
            owner = fakeUser,
            name = "bill1",
            amount = "2",
            varyingAmount = false,
            payoffAmount = null,
            sharedUsers = listOf(
                User(
                    id = 2,
                    uuid = "randomuuid1",
                    name = "random",
                    email = "random@email.com",
                    dateUpdated = 1,
                    dateCreated = 1
                )
            ),
            categories = listOf(
                Category(
                    id = 1,
                    owner = fakeUser,
                    name = "cat2",
                    dateUpdated = 1,
                    dateCreated = 1
                )
            ),
            color = Color(id = 1, red = 255, green = 150, blue = 255, alpha = 255, dateUpdated = 1, dateCreated = 1)
        )

        firstItem.diff(secondItem).updates(fakeUser).run {
            this shouldBe listOf(
                History(null, "Bill 1 amount", "1.23", "2", fakeUser, this[0].dateCreated, this[0].dateUpdated),
                History(
                    null,
                    "Bill 1 sharedUser",
                    null,
                    User(2, "randomuuid1", "random", "random@email.com", null, null, null, null, 1, 1).toJson(),
                    fakeUser,
                    this[1].dateCreated,
                    this[1].dateUpdated
                ),
                History(null, "Category 1 name", "cat1", "cat2", fakeUser, this[2].dateCreated, this[2].dateUpdated),
                History(null, "Color 1 green", "255", "150", fakeUser, this[3].dateCreated, this[3].dateUpdated)
            )
        }
    }

    @Test
    fun `verify diffing Bill with added and removed sharedUsers`() {
        val firstItem = Bill(
            id = 1,
            owner = fakeUser,
            name = "bill1",
            amount = "1.23",
            varyingAmount = false,
            payoffAmount = null,
            sharedUsers = listOf(
                User(
                    id = 3,
                    uuid = "randomuuid2",
                    name = "random",
                    email = "random@email.com",
                    dateUpdated = 1,
                    dateCreated = 1
                )
            ),
            categories = listOf(
                Category(
                    id = 1,
                    owner = fakeUser,
                    name = "cat1",
                    dateUpdated = 1,
                    dateCreated = 1
                )
            ),
            color = Color(id = 1, red = 255, green = 255, blue = 255, alpha = 255, dateUpdated = 1, dateCreated = 1)
        )

        val secondItem = Bill(
            id = 1,
            owner = fakeUser,
            name = "bill1",
            amount = "1.23",
            varyingAmount = false,
            payoffAmount = null,
            sharedUsers = listOf(
                User(
                    id = 2,
                    uuid = "randomuuid1",
                    name = "random",
                    email = "random@email.com",
                    dateUpdated = 1,
                    dateCreated = 1
                )
            ),
            categories = listOf(
                Category(
                    id = 1,
                    owner = fakeUser,
                    name = "cat1",
                    dateUpdated = 1,
                    dateCreated = 1
                )
            ),
            color = Color(id = 1, red = 255, green = 255, blue = 255, alpha = 255, dateUpdated = 1, dateCreated = 1)
        )

        firstItem.diff(secondItem).updates(fakeUser).run {
            this shouldBe listOf(
                History(
                    null,
                    "Bill 1 sharedUser",
                    User(3, "randomuuid2", "random", "random@email.com", null, null, null, null, 1, 1).toJson(),
                    User(2, "randomuuid1", "random", "random@email.com", null, null, null, null, 1, 1).toJson(),
                    fakeUser,
                    this[0].dateCreated,
                    this[0].dateUpdated
                )
            )
        }
    }

    @Test
    fun `verify diffing Bill with added multiple sharedUsers`() {
        val firstItem = Bill(
            id = 1,
            owner = fakeUser,
            name = "bill1",
            amount = "1.23",
            varyingAmount = false,
            payoffAmount = null,
            sharedUsers = null,
            categories = listOf(
                Category(
                    id = 1,
                    owner = fakeUser,
                    name = "cat1",
                    dateUpdated = 1,
                    dateCreated = 1
                )
            ),
            color = Color(id = 1, red = 255, green = 255, blue = 255, alpha = 255, dateUpdated = 1, dateCreated = 1)
        )

        val secondItem = Bill(
            id = 1,
            owner = fakeUser,
            name = "bill1",
            amount = "1.23",
            varyingAmount = false,
            payoffAmount = null,
            sharedUsers = listOf(
                User(
                    id = 2,
                    uuid = "randomuuid1",
                    name = "random",
                    email = "random@email.com",
                    dateUpdated = 1,
                    dateCreated = 1
                ),
                User(
                    id = 3,
                    uuid = "randomuuid2",
                    name = "random",
                    email = "random@email.com",
                    dateUpdated = 1,
                    dateCreated = 1
                )
            ),
            categories = listOf(
                Category(
                    id = 1,
                    owner = fakeUser,
                    name = "cat1",
                    dateUpdated = 1,
                    dateCreated = 1
                )
            ),
            color = Color(id = 1, red = 255, green = 255, blue = 255, alpha = 255, dateUpdated = 1, dateCreated = 1)
        )

        firstItem.diff(secondItem).updates(fakeUser).run {
            this shouldBe listOf(
                History(
                    null,
                    "Bill 1 sharedUser",
                    null,
                    User(3, "randomuuid2", "random", "random@email.com", null, null, null, null, 1, 1).toJson(),
                    fakeUser,
                    this[0].dateCreated,
                    this[0].dateUpdated
                ),
                History(
                    null,
                    "Bill 1 sharedUser",
                    null,
                    User(2, "randomuuid1", "random", "random@email.com", null, null, null, null, 1, 1).toJson(),
                    fakeUser,
                    this[1].dateCreated,
                    this[1].dateUpdated
                )
            )
        }
    }

    @Test
    fun `verify diffing Bill with removing multiple sharedUsers`() {
        val firstItem = Bill(
            id = 1,
            owner = fakeUser,
            name = "bill1",
            amount = "1.23",
            varyingAmount = false,
            payoffAmount = null,
            sharedUsers = listOf(
                User(
                    id = 2,
                    uuid = "randomuuid1",
                    name = "random",
                    email = "random@email.com",
                    dateUpdated = 1,
                    dateCreated = 1
                ),
                User(
                    id = 3,
                    uuid = "randomuuid2",
                    name = "random",
                    email = "random@email.com",
                    dateUpdated = 1,
                    dateCreated = 1
                )
            ),
            categories = listOf(
                Category(
                    id = 1,
                    owner = fakeUser,
                    name = "cat1",
                    dateUpdated = 1,
                    dateCreated = 1
                )
            ),
            color = Color(id = 1, red = 255, green = 255, blue = 255, alpha = 255, dateUpdated = 1, dateCreated = 1)
        )

        val secondItem = Bill(
            id = 1,
            owner = fakeUser,
            name = "bill1",
            amount = "1.23",
            varyingAmount = false,
            payoffAmount = null,
            sharedUsers = null,
            categories = listOf(
                Category(
                    id = 1,
                    owner = fakeUser,
                    name = "cat1",
                    dateUpdated = 1,
                    dateCreated = 1
                )
            ),
            color = Color(id = 1, red = 255, green = 255, blue = 255, alpha = 255, dateUpdated = 1, dateCreated = 1)
        )

        firstItem.diff(secondItem).updates(fakeUser).run {
            this shouldBe listOf(
                History(
                    null,
                    "Bill 1 sharedUser",
                    User(3, "randomuuid2", "random", "random@email.com", null, null, null, null, 1, 1).toJson(),
                    null,
                    fakeUser,
                    this[0].dateCreated,
                    this[0].dateUpdated
                ),
                History(
                    null,
                    "Bill 1 sharedUser",
                    User(2, "randomuuid1", "random", "random@email.com", null, null, null, null, 1, 1).toJson(),
                    null,
                    fakeUser,
                    this[1].dateCreated,
                    this[1].dateUpdated
                )
            )
        }
    }

    @Test
    fun `verify diffing list of Generic item`() {
        val firstList = listOf(
            Random(1, "1"),
            Random(2, "2"),
            Random(3, "3")
        )

        val secondList = listOf(
            Random(1, "1"),
            Random(2, "4"),
            Random(3, "3")
        )

        firstList.diff(secondList).apply {
            this.added shouldBe emptyList()
            this.removed shouldBe emptyList()
            this.updated shouldBe listOf(2)
        }
    }

    @Test
    fun `verify diffing list of Generic item with different sizes`() {
        val firstList = listOf(
            Random(1, "1"),
            Random(2, "2"),
            Random(3, "3")
        )

        val secondList = listOf(
            Random(1, "1"),
            Random(2, "4"),
            Random(name = "2"),
            Random(name = "5")
        )

        firstList.diff(secondList).apply {
            this.added shouldBe listOf(
                Random(name = "2"),
                Random(name = "5")
            )
            this.removed shouldBe listOf(
                Random(3, "3")
            )
            this.updated shouldBe listOf(2)
        }
    }

    @Test
    fun `verify diffing list of Generic item removing all`() {
        val firstList = listOf(
            Random(1, "1"),
            Random(2, "2"),
            Random(3, "3")
        )

        val secondList = null

        firstList.diff(secondList).apply {
            this.added shouldBe emptyList()
            this.removed shouldBe listOf(
                Random(1, "1"),
                Random(2, "2"),
                Random(3, "3")
            )
            this.updated shouldBe emptyList()
        }
    }
}
