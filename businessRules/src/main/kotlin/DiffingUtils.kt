import shared.auth.User
import shared.base.GenericItem
import shared.base.History
import shared.base.HistoryItem
import kotlin.reflect.full.declaredMemberProperties

/**
 * A class to hold the differences between and item from one state to another.
 *
 * @param added The items that where added to the item.
 * @param removed The items that where removed from the item.
 */
data class ItemDiff(val added: List<Pair<String, Any?>>, val removed: List<Pair<String, Any?>>) {
    private val addedDiff: Int
        get() {
            val diff = added.size - removed.size
            return if (diff < 0) 0 else diff
        }

    private val removedDiff: Int
        get() {
            val diff = removed.size - added.size
            return if (diff < 0) 0 else diff
        }

    private fun List<Pair<String, Any?>>.mapExtras(diff: Int, updatedBy: User, added: Boolean = true): List<History> {
        val extras = this.subList(this.size - diff, this.size)

        return extras.map {
            History(
                field = it.first,
                oldValue = if (added) null else it.second,
                newValue = if (added) it.second else null,
                updatedBy = updatedBy
            )
        }
    }

    private fun List<Pair<String, Any?>>.sorted(diff: Int) = this.subList(0, this.size - diff).sortedBy { it.first }

    /**
     * Converts the [added] and [removed] lists to a cohesive list of the updates as a list of [History].F
     */
    fun updates(updatedBy: User): List<History> {
        val history = mutableListOf<History>()

        if (addedDiff > 0) history += added.mapExtras(addedDiff, updatedBy)
        if (removedDiff > 0) history += removed.mapExtras(removedDiff, updatedBy, false)

        val addSorted = added.sorted(addedDiff)
        val removeSorted = removed.sorted(removedDiff)

        history += addSorted.mapIndexed { index, pair ->
            val remove = removeSorted[index]

            History(field = pair.first, oldValue = remove.second, newValue = pair.second, updatedBy = updatedBy)
        }

        return history.sortedBy { it.field }
    }
}

/**
 * Splits the [GenericItem] generic item in to a list of its parameters names and their values.
 */
fun GenericItem.separate(): List<Pair<String, Any?>> {
    val split = mutableListOf<Pair<String, Any?>>()
    this.javaClass.kotlin.declaredMemberProperties.forEach {
        if (
            it.name == HistoryItem::history.name
            || it.name == "owner"
            || it.name == GenericItem::dateCreated.name
            || it.name == GenericItem::dateUpdated.name
        ) return@forEach


        when (val item = it.get(this)) {
            is GenericItem -> split += item.separate()
            is List<*> -> item.filterIsInstance<GenericItem>().forEach { listItem ->
                if (listItem is User) split.add(Pair("${this::class.simpleName} $id sharedUser", listItem))
                else split += listItem.separate()
            }
            else -> {
                if (it.name == "sharedUsers") split.add(Pair("${this::class.simpleName} $id sharedUser", null))
                else split.add(Pair("${this::class.simpleName} $id ${it.name}", item))
            }
        }
    }

    return split.toList().sortedBy { it.first }
}

/**
 * Diff's two [O] items, for the [user].
 *
 * @return [ItemDiff], of the differences between [O] and [other].
 */
inline fun <reified O : GenericItem> O.diff(other: O): ItemDiff {
    val firstItem = this.separate()
    val secondItem = other.separate()

    val removed = firstItem - secondItem
    val added = secondItem - firstItem

    return ItemDiff(added, removed)
}

data class ListDiff<O : GenericItem>(val added: List<O>, val removed: List<O>, val updated: List<Int>)

/**
 * Diff's two [O] items, for the [user].
 *
 * @return [ItemDiff], of the differences between [O] and [other].
 */
inline fun <reified O : GenericItem> List<O>?.diff(other: List<O>?): ListDiff<O> {
    val removed = this.diffLists(other)
    val added = other.diffLists(this)
    val updated = mutableListOf<Int>()

    if (added.isNotEmpty())
        updated += added.mapNotNull { add -> removed.find { it.id == add.id }?.id }.also { items ->
            if (items.isNotEmpty()) {
                added.removeIf { items.contains(it.id) }
                removed.removeIf { items.contains(it.id) }
            }
        }

    if (removed.isNotEmpty())
        updated += removed.mapNotNull { remove -> added.find { it.id == remove.id }?.id }.also { items ->
            if (items.isNotEmpty()) {
                added.removeIf { items.contains(it.id) }
                removed.removeIf { items.contains(it.id) }
            }
        }

    return ListDiff(added, removed, updated)
}

/**
 * Used internally to diff potentially nullable lists Should not use within the app itself. Needing the reified class
 * makes it accessible anywhere.
 */
inline fun <reified O : GenericItem> List<O>?.diffLists(other: List<O>?): MutableList<O> =
    ((this ?: listOf()) - (other ?: listOf())) as MutableList<O>
