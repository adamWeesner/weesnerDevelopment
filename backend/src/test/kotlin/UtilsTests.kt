import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import shared.auth.User
import shared.base.History
import shared.taxFetcher.MaritalStatus
import shared.taxFetcher.Medicare
import shared.taxFetcher.MedicareLimit
import shared.taxFetcher.SocialSecurity

open class UtilsTests : StringSpec({
    val fakeUser = User(name = "test", email = "test@email.com", dateUpdated = 1, dateCreated = 1)

    "verify diffing Social Security item" {
        val firstItem = SocialSecurity(1, 2020, 3.4, 128000, null, 1, 1)
        val secondItem = SocialSecurity(1, 2020, 3.6, 127500, null, 1, 1)

        val diff = firstItem.diff(secondItem, fakeUser)

        diff shouldBe listOf(
            History(null, "SocialSecurity 1 limit", 128000, 127500, fakeUser, diff[0].dateCreated, diff[0].dateUpdated),
            History(null, "SocialSecurity 1 percent", 3.4, 3.6, fakeUser, diff[1].dateCreated, diff[1].dateUpdated)
        )
    }

    "verify diffing Social Security item with history" {
        val firstItem = SocialSecurity(1, 2020, 3.4, 128000, listOf(History(1, "", "", "", fakeUser)), 1, 1)
        val secondItem = SocialSecurity(1, 2020, 3.6, 127500, null, 1, 1)

        val diff = firstItem.diff(secondItem, fakeUser)

        diff shouldBe listOf(
            History(null, "SocialSecurity 1 limit", 128000, 127500, fakeUser, diff[0].dateCreated, diff[0].dateUpdated),
            History(null, "SocialSecurity 1 percent", 3.4, 3.6, fakeUser, diff[1].dateCreated, diff[1].dateUpdated)
        )
    }

    "verify diffing Medicare item" {
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

        val diff = firstItem.diff(secondItem, fakeUser)

        diff shouldBe listOf(
            History(null, "Medicare 1 additionalPercent", 1.1, 1.2, fakeUser, diff[0].dateCreated, diff[0].dateUpdated),
            History(null, "MedicareLimit 1 amount", 123, 124, fakeUser, diff[1].dateCreated, diff[1].dateUpdated),
            History(null, "MedicareLimit 3 amount", 345, 346, fakeUser, diff[2].dateCreated, diff[2].dateUpdated)
        )
    }
})
