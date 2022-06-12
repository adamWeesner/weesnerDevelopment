package com.weesnerdevelopment.auth

import com.weesnerdevelopment.auth.exposed.UserTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach

abstract class AuthTests {
    abstract val baseUrl: String
    internal val config = "application-test.conf"

    sealed class Testing {
        @AfterEach
        fun cleanUp() {
            transaction {
                SchemaUtils.drop(
                    UserTable,
                )
                SchemaUtils.create(
                    UserTable,
                )
            }
        }
    }
}