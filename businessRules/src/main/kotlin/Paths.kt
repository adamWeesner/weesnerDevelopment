import Path.BillMan.basePath
import Path.BreathOfTheWild.basePath
import Path.TaxFetcher.basePath
import Path.User.base

/**
 * The available paths at /[Path].
 */
sealed class Path {
    object Server : Path() {
        val health = "health"
        val validation = "validation"
        val complexValidation = "complexValidation"
    }

    /**
     * The available paths at [basePath]/value.
     */
    object TaxFetcher : Path() {
        private val basePath = "taxFetcher/"
        val socialSecurity = "${basePath}socialSecurity"
        val medicare = "${basePath}medicare"
        val federalIncomeTax = "${basePath}federalIncomeTax"
        val taxWithholding = "${basePath}taxWithholding"
    }

    /**
     * The available paths at [base]/value.
     */
    object User : Path() {
        const val base = "user"
        const val account = "/account"
        const val login = "/login"
        const val signUp = "/signUp"
    }

    /**
     * The available paths at [basePath]/value.
     */
    object BillMan : Path() {
        val basePath = "billMan/"
        val bills = "${basePath}bills"
        val categories = "${basePath}categories"
        val income = "${basePath}income"
        val occurrences = "$bills/occurrences"
        val incomeOccurrences = "$income/occurrences"
        val logging = "${basePath}logging"
    }

    /**
     * The available paths at [basePath]/value.
     */
    object BreathOfTheWild : Path() {
        val basePath = "breathOfTheWild/"
        val all = "${basePath}all"
        val critters = "${basePath}critters"
    }
}
