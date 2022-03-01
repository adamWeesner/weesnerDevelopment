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