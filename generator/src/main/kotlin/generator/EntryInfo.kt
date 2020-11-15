package generator

import java.io.File

data class EntryInfo(
    val baseDirectory: File,
    val className: String,
    val data: List<String>
) {
    val lowered = className.decapitalize()
    val subDirectory = File("${baseDirectory.path}/$lowered")

    init {
        subDirectory.mkdirs()
    }
}
