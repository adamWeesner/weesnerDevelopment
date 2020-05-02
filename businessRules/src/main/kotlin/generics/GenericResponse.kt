package generics

/**
 * Generic response to generate a json array from a list of [T] returned from the backend.
 */
interface GenericResponse<T> {
    var items: List<T>?
}
