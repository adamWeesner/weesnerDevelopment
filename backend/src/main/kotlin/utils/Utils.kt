package com.weesnerdevelopment.utils

import com.weesnerdevelopment.utils.Path.BillMan.basePath
import com.weesnerdevelopment.utils.Path.TaxFetcher.basePath
import com.weesnerdevelopment.utils.Path.User.base

/**
 * The available paths at /[Path].
 */
sealed class Path {
    object Server : Path() {
        val health = "health"
        val validation = "validation"
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
        val occurrences = "${basePath}occurrences"
        val incomeOccurrences = "${income}/occurrences"
    }
}
