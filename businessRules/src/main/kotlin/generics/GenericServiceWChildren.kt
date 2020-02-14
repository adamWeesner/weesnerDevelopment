package generics

import model.Notification

abstract class GenericServiceWChildren<O : GenericItem, T : IdTable>(
    override val table: T
) : GenericService<O, T>(table) {
    var childServices: MutableList<GenericService<GenericItem, IdTable>> = mutableListOf()
    private val listeners = mutableMapOf<Int, suspend (Notification<O?>) -> Unit>()

    override fun addChangeListener(id: Int, listener: suspend (Notification<O?>) -> Unit) {
        listeners[id] = listener

        repeat(childServices.size) { addChangeListener(id, listener) }
    }

    override fun removeChangeListener(id: Int): (suspend (Notification<O?>) -> Unit)? {
        repeat(childServices.size) { removeChangeListener(id) }

        return listeners.remove(id)
    }
}