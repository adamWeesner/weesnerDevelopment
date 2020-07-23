import shared.base.OwnedItem

inline fun <reified T : OwnedItem> List<T>.forOwner(uuid: String?) =
    filter { it.owner.uuid == uuid }