package generics

abstract class GenericResponse<T : GenericItem>(
    open var items: List<T>?
)