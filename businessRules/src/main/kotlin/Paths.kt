import Path.BillMan.basePath
import Path.BreathOfTheWild.basePath
import Path.SerialCabinet.basePath
import Path.TaxFetcher.basePath

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
        private const val version = "v1"
        const val basePath = "$version/user"
        const val health = "${basePath}/health"
        const val account = "${basePath}/account"
        const val login = "${basePath}/login"
        const val info = "${basePath}/info"
        const val signUp = "${basePath}/signUp"
        const val logging = "${basePath}/logging"
    }

    /**
     * The available paths at [basePath]/value.
     */
    object BillMan : Path() {
        private const val version = "v1"
        const val basePath = "$version/billMan/"
        const val health = "${basePath}health"
        const val bills = "${basePath}bills"
        const val categories = "${basePath}categories"
        const val income = "${basePath}income"
        const val billOccurrences = "$bills/occurrences"
        const val incomeOccurrences = "$income/occurrences"
        const val logging = "${basePath}logging"
    }

    /**
     * The available paths at [basePath]/value.
     */
    object BreathOfTheWild : Path() {
        val basePath = "breathOfTheWild/"
        val all = "${basePath}all"
        val cookingPotFoods = "${basePath}cookingPotFoods"
        val elixirs = "${basePath}elixirs"
        val frozenFoods = "${basePath}frozenFoods"
        val images = "${basePath}images"
        val critters = "${basePath}critters"
        val otherFoods = "${basePath}otherFoods"
        val effects = "${basePath}effects"
        val monsterParts = "${basePath}monsterParts"
        val roastedFoods = "${basePath}roastedFoods"
        val ingredients = "${basePath}ingredients"
    }

    /**
     * The available paths at [basePath]/value.
     */
    object SerialCabinet : Path() {
        val basePath = "serialCabinet/"
        val all = "${basePath}all"
        val manufacturers = "${basePath}manufacturers"
        val categories = "${basePath}categories"
        val electronics = "${basePath}electronics"
    }

}