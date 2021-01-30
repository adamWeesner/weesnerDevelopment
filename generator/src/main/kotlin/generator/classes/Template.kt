package generator.classes

import java.io.File

interface GenTemplate : GenFile {
    fun update(updates: () -> List<Pair<String, String>>): String
}

data class Template(
    override val name: String,
    override val path: String = "generator/src/main/kotlin/templates/$name",
    override val data: String = File(path).readText(),
) : GenTemplate {
    private var updatedData = data

    override fun update(updates: () -> List<Pair<String, String>>): String {
        updates().forEach {
            updatedData = updatedData.replace(it.first, it.second)
        }

        if (updatedData.contains("#")) {
            val missing = updatedData.split(" ").filter { it.startsWith("#") }.toSet()
            throw IllegalStateException("Template still has arguments to override: ${missing.joinToString(", ")}")
        }

        return updatedData
    }
}
