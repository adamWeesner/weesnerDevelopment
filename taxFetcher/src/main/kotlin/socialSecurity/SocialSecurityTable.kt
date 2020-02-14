package socialSecurity

import generics.IdTable

object SocialSecurityTable : IdTable() {
    val year = integer("year").primaryKey()
    val percent = double("percent")
    val limit = integer("limit")
}