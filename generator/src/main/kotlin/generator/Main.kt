package generator

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt

class Generate : CliktCommand() {
    private val title: String by option(help = "The title of the module to generate").prompt("Title")
    private val sharedFolder: String by option(help = "The libs shared folder to generate data based on.").prompt("Shared folder")

    override fun run() {
        echo("Beginning generation...")

        Builder(title, sharedFolder)
    }
}

fun main(args: Array<String>) = Generate().main(args)
