package generics

import model.Notification

abstract class GenericServiceWChildren<O : GenericItem, T : IdTable>(
    override val table: T
) : GenericService<O, T>(table) {
    var childServices: List<Pair<String, GenericService<GenericItem, IdTable>>>? = null
    private val listeners = mutableMapOf<Int, suspend (Notification<O?>) -> Unit>()

    override fun addChangeListener(id: Int, listener: suspend (Notification<O?>) -> Unit) {
        listeners[id] = listener

        childServices?.toList()?.size?.let { repeat(it) { addChangeListener(id, listener) } }
    }

    override fun removeChangeListener(id: Int): (suspend (Notification<O?>) -> Unit)? {
        childServices?.toList()?.size?.let { repeat(it) { removeChangeListener(id) } }

        return listeners.remove(id)
    }
}

class MissingChildService(type: String) : IllegalArgumentException("${type}Service is required")