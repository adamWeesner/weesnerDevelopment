package generator.classes

import com.github.ajalt.clikt.output.TermUi.echo
import generator.saveToFile
import java.io.File

interface GenFile {
    val name: String
    val path: String
    val data: String
}

interface GenFileSavable : GenFile {
    fun save(data: () -> String)
    fun update(info: (List<String>) -> String)
}

data class GeneratorFile(
    override val path: String,
    override val name: String = path.substringAfterLast("/"),
    override val data: String = runCatching { File(path).readText() }.getOrNull() ?: "",
) : GenFileSavable {
    companion object {
        fun open(path: String) = GeneratorFile(path)

        fun create(path: String) = open(path).also {
            if (saveToFile) File(it.path).mkdirs()

            echo("+ ${it.path}")
        }
    }

    override fun save(data: () -> String) {
        if (saveToFile) {
            File(path).writeText(data())
            echo("+ $path")
        } else {
            echo("----- data for $name -----\n")
            echo("${data()}\n")
        }
    }

    override fun update(info: (List<String>) -> String) {
        val updatedData = info(data.split("\n"))
        if (saveToFile) {
            File(path).writeText(updatedData)
            echo("updated $path")
        } else {
            echo("----- data for $name -----\n")
            echo("$updatedData\n")
        }
    }
}
