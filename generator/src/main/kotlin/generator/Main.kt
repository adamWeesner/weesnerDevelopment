package generator

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt

var saveToFile: Boolean = false

class Generate : CliktCommand() {
    private val title: String by option(help = "The title of the module to generate").prompt("Title")
    private val sharedFolder: String by option(help = "The libs shared folder to generate data based on.").prompt("Shared folder")
    private val save by option(help = "Whether to save the created data to files").prompt("Save files?", "false")

    override fun run() {
        saveToFile = save.toBoolean()
        echo("Beginning generation...")

        Builder(title, sharedFolder)
    }
}

fun main(args: Array<String>) = Generate().main(args)
