package com.weesnerdevelopment

import com.weesnerdevelopment.Path.TaxFetcher.basePath
import com.weesnerdevelopment.Path.User.base

/**
 * The available paths at /[Path].
 */
sealed class Path {
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
}
