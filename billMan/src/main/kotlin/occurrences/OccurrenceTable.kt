package occurrences

import org.jetbrains.exposed.sql.Column

/**
 * Table definition for table that has occurrence data for the database.
 */
interface OccurrenceTable {
    val ownerId: Column<String>
    val amount: Column<String>
    val dueDate: Column<Long>
    val itemId: Column<Int>
    val every: Column<String>
}
