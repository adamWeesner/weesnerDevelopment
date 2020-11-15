package generator

import java.io.File
import java.io.FileOutputStream
import java.nio.file.Paths
import java.util.jar.JarEntry
import java.util.jar.JarFile

data class SharedLibInfo(
    val sharedFolder: String,
    val baseDirectory: File,
    val processEntry: (String, List<String>) -> Unit
) {
    private lateinit var sharedDirectory: String
    private lateinit var jarFile: JarFile

    init {
        readLibEntries().processEntries(processEntry)
    }

    private fun readLibEntries(): List<JarEntry> {
        val jar = Paths.get("./libs/").toFile().listFiles()?.first { it.name.matches(Regex(".*-sources.jar")) }
        sharedDirectory = "commonMain/shared/$sharedFolder"
        jarFile = JarFile(jar?.path)
        return jarFile.entries().toList().filter {
            it.name.startsWith(sharedDirectory)
                    && !it.name.startsWith("$sharedDirectory/responses/")
                    && !it.isDirectory
        }
    }

    private fun List<JarEntry>.processEntries(entryData: (String, List<String>) -> Unit) {
        forEach {
            val fileName = it.name.replace("$sharedDirectory/", "")
            val file = File("${baseDirectory.path}/$fileName")
            val inputStream = jarFile.getInputStream(it)
            val outputStream = FileOutputStream(file)

            while (inputStream.available() > 0) {
                outputStream.write(inputStream.read())
            }

            file.processEntry(entryData)
        }
    }

    private fun File.processEntry(entryData: (String, List<String>) -> Unit) {
        var className = ""
        val data = readLines().map { data ->
            when {
                data.startsWith("data class") -> {
                    className = data.replace("data class ", "").replace("(", "")

                    data
                }
                data.contains(Regex("import .*")) ||
                        data.contains(Regex("override .*")) ||
                        data.contains(Regex("import .*")) ||
                        data.contains("package shared.$sharedFolder") ||
                        data.contains("@Parcelize") -> ""
                else -> data
            }
        }.filter { it.isNotBlank() }

        val stringData = data.drop(1).dropLast(1).map { it.replace(",", "").replace("val ", "").trim() }

        delete()
        entryData(className, stringData)
    }
}
