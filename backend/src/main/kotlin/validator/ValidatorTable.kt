package com.weesnerdevelopment.validator

import generics.IdTable

object ValidatorTable : IdTable() {
    val name = varchar("name", 255).uniqueIndex()
    val amount = double("amount")
}
