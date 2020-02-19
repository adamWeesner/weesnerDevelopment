import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/**
 * Helper function to query [T] in the table.
 */
suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction { block() }
