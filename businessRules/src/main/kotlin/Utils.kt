import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.lang.reflect.ParameterizedType

/**
 * Helper function to query [T] in the table.
 */
suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction { block() }

/**
 * A moshi instance that can take a [ParameterizedType] if needed.
 */
inline fun <reified T> moshi(type: ParameterizedType? = null) =
    Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter<T>(type ?: T::class.java)

/**
 * Helper function that creates an object of type [T] from a json string.
 */
inline fun <reified T> String.fromJson(type: ParameterizedType? = null) = moshi<T>(type).fromJson(this)

/**
 * Helper function to convert an object to a json string.
 */
inline fun <reified T> T?.toJson() = moshi<T>().toJson(this)
