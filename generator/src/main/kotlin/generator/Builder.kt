package generator

import com.github.ajalt.clikt.output.TermUi.echo
import java.io.File

data class Builder(
    val title: String,
    val sharedFolder: String
) {
    private lateinit var baseDirectory: File
    private lateinit var entryInfo: EntryInfo

    private val lowerName = title.split(" ").mapIndexed { index, item ->
        if (index == 0) item.toLowerCase()
        else item.capitalize()
    }.joinToString("")

    init {
        createBaseDirectory()
        createGradleFile()

        SharedLibInfo(sharedFolder, baseDirectory) { className, data ->
            entryInfo = EntryInfo(baseDirectory, className, data)

            generateTable()
            generateService()
            generateRouter()

        }
        generateRoutes(lowerName.capitalize(), entryInfo)
        updateSettingsGradle(lowerName)
    }

    private fun createBaseDirectory() {
        baseDirectory = File("$lowerName/src/main/kotlin/$lowerName")
        baseDirectory.mkdirs()
        echo("+ ${baseDirectory.path}")
    }

    private fun createGradleFile() {
        val gradleFile = File("$lowerName/build.gradle.kts")
        val gradleFileData = """import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id(Kotlin.jvm)
    id(Kotlin.kapt)
}

group = Base.group
version = $lowerName.version

sourceSets { sharedSources() }
repositories { sharedRepos() }
java { javaSource() }
tasks.withType<KotlinCompile>().all { kotlinOptions.jvmTarget = Jvm.version }

dependencies {
    implementation(project(BusinessRules.project))
}
"""

        gradleFile.writeText(gradleFileData)
        echo("+ ${gradleFile.path}")
    }

    private fun generateTable() {
        val file = File("${entryInfo.subDirectory.path}/${entryInfo.className}sTable.kt")
        val tableInfo = entryInfo.data.map {
            val name = it.substringBefore(":")
            val typeName = it.substringAfter(":").trim()
            val type = when {
                typeName == "String" -> "varchar(\"$name\", 255)"
                typeName == "String?" -> "varchar(\"$name\", 255).nullable()"
                typeName == "Int" -> "integer(\"$name\")"
                typeName == "Int?" -> "integer(\"$name\").nullable()"
                typeName.matches(Regex("List<.*>\\??")) -> {
                    val nullable = typeName.endsWith("?")
                    val typeLabeled = when (
                        val typeTrimmed = typeName
                            .replace(Regex("List<"), "")
                            .replace(Regex(">\\??"), "")
                        ) {
                        "String", "String?", "Int", "Int?" -> {
                            val named = if (name.endsWith("s")) name.dropLast(1) else name
                            "$named: $typeTrimmed"
                        }
                        else -> "${typeTrimmed.decapitalize()}Id"
                    }
                    val subEntryInfo = EntryInfo(
                        baseDirectory,
                        "${entryInfo.className}${name.capitalize()}",
                        listOf(typeLabeled, "itemId: ${entryInfo.className}")
                    )

                    generateRelationshipClass(subEntryInfo)
                    generateRelationshipTable(subEntryInfo, entryInfo.className)
                    generateRelationshipService(subEntryInfo, nullable)
                    null
                }
                else -> "reference(\"${typeName.decapitalize()}Id\", ${typeName}sTable.id)"
            }
            if (type == null) null
            else "val $name = $type"
        }

        val dataString = """package $title.${entryInfo.lowered}

import generics.IdTable
${if (tableInfo.any { it?.contains("ImagesTable") == true }) "import $title.image.ImagesTable" else ""}

object ${entryInfo.className}sTable : IdTable() {
    ${tableInfo.filterNotNull().joinToString("\n    ")}
}
"""

        file.writeText(dataString)
        echo("+ ${file.path}")
    }

    private fun generateService() {
        val file = File("${entryInfo.subDirectory.path}/${entryInfo.className}sService.kt")
        val tableInfo = entryInfo.data.map {
            val name = it.substringBefore(":")
            "row[table.$name]," to "this[table.$name] = item.$name"
        }

        val dataString = """package $title.${entryInfo.lowered}

import BaseService
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.$sharedFolder.${entryInfo.className}

class ${entryInfo.className}sService : BaseService<${entryInfo.className}sTable, ${entryInfo.className}>(
    ${entryInfo.className}sTable
) {
    override val ${entryInfo.className}sTable.connections: Join?
        get() = null

    override suspend fun toItem(row: ResultRow) = ${entryInfo.className}(
        row[table.id],
        ${tableInfo.joinToString("\n        ") { it.first }}
        row[table.dateCreated],
        row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: ${entryInfo.className}) {
        ${tableInfo.joinToString("\n        ") { it.second }}
    }
}
"""

        file.writeText(dataString)
        echo("+ ${file.path}")
    }

    private fun generateRouter() {
        val file = File("${entryInfo.subDirectory.path}/${entryInfo.className}sRouter.kt")

        val dataString = """package $title.${entryInfo.lowered}

import BaseRouter
import shared.$sharedFolder.${entryInfo.className}
import shared.$sharedFolder.responses.${entryInfo.className}sResponse
import kotlin.reflect.full.createType

data class ${entryInfo.className}sRouter(
    override val basePath: String,
    override val service: ${entryInfo.className}sService
) : BaseRouter<${entryInfo.className}, ${entryInfo.className}sService>(
    ${entryInfo.className}sResponse(),
    service,
    ${entryInfo.className}::class.createType()
)
"""

        file.writeText(dataString)
        echo("+ ${file.path}")
    }

    private fun generateRelationshipClass(entryInfo: EntryInfo) {
        val trimmedName =
            if (entryInfo.className.endsWith("s")) entryInfo.className.dropLast(1) else entryInfo.className
        val file = File("${entryInfo.subDirectory.path}/${trimmedName}.kt")

        val tableInfo = entryInfo.data.map {
            val name = it.substringBefore(":")
            val type = when (val typeName = it.substringAfter(":").trim()) {
                "String", "String?", "Int", "Int?" -> typeName
                else -> "Int"
            }
            "val $name: $type"
        }
        val dataString = """package $title.${entryInfo.lowered}

import shared.base.GenericItem
import shared.currentTimeMillis

data class $trimmedName(
    override val id: Int?,
    ${tableInfo.joinToString(",\n    ")},
    override val dateCreated: Long = currentTimeMillis(),
    override val dateUpdated: Long = currentTimeMillis()
): GenericItem
"""

        file.writeText(dataString)
        echo("+ ${file.path}")
    }

    private fun generateRelationshipTable(entryInfo: EntryInfo, baseLocation: String) {
        val file = File("${entryInfo.subDirectory.path}/${entryInfo.className}Table.kt")
        val tableInfo = entryInfo.data.map {
            val name = it.substringBefore(":")
            val type = when (val typeName = it.substringAfter(":").trim()) {
                "String" -> "varchar(\"$name\", 255)"
                "String?" -> "varchar(\"$name\", 255).nullable()"
                "Int" -> "integer(\"$name\")"
                "Int?" -> "integer(\"$name\").nullable()"
                else -> {
                    val item = if (typeName.endsWith("Id")) typeName.dropLast(2).capitalize() else typeName
                    "reference(\"${typeName.decapitalize()}\", ${item.capitalize()}sTable.id, ReferenceOption.CASCADE)"
                }
            }
            "val $name = $type"
        }

        val dataString = """package $title.${entryInfo.lowered}

import $title.${baseLocation.decapitalize()}.${baseLocation.capitalize()}sTable
import generics.IdTable
import org.jetbrains.exposed.sql.ReferenceOption

object ${entryInfo.className}Table : IdTable() {
    ${tableInfo.joinToString("\n    ")}
}
"""

        file.writeText(dataString)
        echo("+ ${file.path}")
    }

    private fun generateRelationshipService(entryInfo: EntryInfo, nullable: Boolean) {
        val trimmedName =
            if (entryInfo.className.endsWith("s")) entryInfo.className.dropLast(1) else entryInfo.className

        val file = File("${entryInfo.subDirectory.path}/${entryInfo.className}Service.kt")
        val tableInfo = entryInfo.data.map {
            val name = it.substringBefore(":")
            "row[table.$name]," to "this[table.$name] = item.$name"
        }

        val dataString = """package $title.${entryInfo.lowered}

import BaseService
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class ${entryInfo.className}Service : BaseService<${entryInfo.className}Table, $trimmedName>(
    ${entryInfo.className}Table
) {
    override val ${entryInfo.className}Table.connections: Join?
        get() = null
        
    suspend fun getFor(id: Int) = getAll {
        ${entryInfo.className}Table.itemId eq id
    }?.map { toItem(it) }${if (nullable) "?: throw InvalidAttributeException(\"${entryInfo.className}\")" else "!!"}

    override suspend fun toItem(row: ResultRow) = $trimmedName(
        row[table.id],
        ${tableInfo.joinToString("\n        ") { it.first }}
        row[table.dateCreated],
        row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: $trimmedName) {
        ${tableInfo.joinToString("\n        ") { it.second }}
    }
}
"""

        file.writeText(dataString)
        echo("+ ${file.path}")
    }

    private fun generateRoutes(title: String, entryInfo: EntryInfo) {
        val file = File("./backend/src/main/kotlin/routes/${title}Routes.kt")
        val routers = arrayListOf<File>()
        val services = arrayListOf<File>()
        val tables = arrayListOf<File>()

        entryInfo.baseDirectory.listFiles()?.forEach {
            if (it.isDirectory) it.listFiles()?.forEach {
                if (it.name.endsWith("Router.kt")) routers.add(it)
                if (it.name.endsWith("Service.kt")) services.add(it)
                if (it.name.endsWith("Table.kt")) tables.add(it)
            }
        }

        val names = routers.map { it.name.decapitalize().replace(".kt", "") }

        val dataString = """package com.weesnerdevelopment.routes

${
            routers.joinToString("\n") {
                "import ${
                    it.path
                        .replace("${title.decapitalize()}/src/main/kotlin/", "")
                        .replace("/", ".")
                        .replace(".kt", "")
                }"
            }
        }
import io.ktor.auth.*
import io.ktor.routing.*
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein

fun Routing.${title.decapitalize()}Routes() {
    ${names.joinToString("\n    ") { "val $it by kodein().instance<${it.capitalize()}>()" }}

    ${names.joinToString("\n\n    ") { "${it}.apply {\n        authenticate {\n            setupRoutes()\n        }\n   }" }}
}
        """

        file.writeText(dataString)

        updateInjectionRouters(lowerName, routers)
        updateInjectionServices(lowerName, services)
        updatePaths(lowerName, routers)
        updateDatabaseFactory(tables)
        echo("+ ${file.path}")
    }

    private fun updateInjectionRouters(baseDir: String, routers: List<File>) {
        val file = File("./backend/src/main/kotlin/injection/Routers.kt")
        var lastLine = ""
        val updatedFileData = StringBuilder()

        val imports = routers.joinToString("\n") {
            "import ${
                it.path
                    .replace("${title.decapitalize()}/src/main/kotlin/", "")
                    .replace("/", ".")
                    .replace(".kt", "")
            }"
        }

        val bindings = routers.joinToString("\n") {
            val trimmedName = it.name.replace(".kt", "")
            val className = trimmedName.replace("Router", "").decapitalize()
            "    bind<$trimmedName>() with singleton { $trimmedName(${baseDir.capitalize()}.$className, instance()) }"
        }

        file.readLines().forEachIndexed { index, line ->
            if (lastLine.trim().startsWith("import ") && line.trim() == "") {
                updatedFileData.append(imports).append("\n\n")
            } else if (lastLine.trim().startsWith("bind<") && line.trim() == "}") {
                updatedFileData.append("\n    // $baseDir\n")
                updatedFileData.append(bindings).append("\n}")
            } else {
                updatedFileData.append(line).append("\n")
            }

            lastLine = line
        }

        file.writeText(updatedFileData.toString())
        echo("updated ${file.path}")
    }

    private fun updateInjectionServices(baseDir: String, services: List<File>) {
        val file = File("./backend/src/main/kotlin/injection/Services.kt")
        var lastLine = ""
        val updatedFileData = StringBuilder()

        val imports = services.joinToString("\n") {
            "import ${
                it.path
                    .replace("${title.decapitalize()}/src/main/kotlin/", "")
                    .replace("/", ".")
                    .replace(".kt", "")
            }"
        }

        val bindings = services.joinToString("\n") {
            val trimmedName = it.name.replace(".kt", "")
            "    bind<$trimmedName>() with singleton { $trimmedName(instance()) }"
        }

        file.readLines().forEachIndexed { index, line ->
            if (lastLine.trim().startsWith("import ") && line.trim() == "") {
                updatedFileData.append(imports).append("\n\n")
            } else if (lastLine.trim().startsWith("bind<") && line.trim() == "}") {
                updatedFileData.append("\n    // $baseDir\n")
                updatedFileData.append(bindings).append("\n}")
            } else {
                updatedFileData.append(line).append("\n")
            }

            lastLine = line
        }

        file.writeText(updatedFileData.toString())
        echo("updated ${file.path}")
    }

    private fun updatePaths(baseDir: String, routers: List<File>) {
        val file = File("./businessRules/src/main/kotlin/Paths.kt")
        var lastLine = ""
        val updatedFileData = StringBuilder()

        val routes = routers.map {
            it.path
                .replace("${title.decapitalize()}/src/main/kotlin/", "")
                .replace("/", ".")
                .replace("Router.kt", "")
                .split(".")[1]
                .decapitalize()
        }

        val data = """
    /**
     * The available paths at [basePath]/value.
     */
     object ${baseDir.capitalize()} : Path() {
        val basePath = "$baseDir/"
        val all = "${"\${basePath}"}all"
${routes.joinToString("\n") { "        val $it = \"${"\${basePath}"}$it\"" }}
    }"""

        file.readLines().forEach { line ->
            if (lastLine.trim().startsWith("import ") && line.trim() == "") {
                updatedFileData.append("import Path.${baseDir.capitalize()}.basePath").append("\n\n")
            } else if (lastLine == "    }" && line == "}") {
                updatedFileData.append(data).append("\n}")
            } else {
                updatedFileData.append(line).append("\n")
            }

            lastLine = line
        }

        file.writeText(updatedFileData.toString())
        echo("updated ${file.path}")
    }

    private fun updateDatabaseFactory(tables: List<File>) {
        val file = File("./backend/src/main/kotlin/service/DatabaseFactory.kt")
        var lastLine = ""
        val updatedFileData = StringBuilder()

        val imports = tables.joinToString("\n") {
            "import ${
                it.path
                    .replace("${title.decapitalize()}/src/main/kotlin/", "")
                    .replace("/", ".")
                    .replace(".kt", "")
            }"
        }

        val routes = tables.map {
            it.path
                .replace("${title.decapitalize()}/src/main/kotlin/", "")
                .replace("/", ".")
                .replace("kt", "")
                .split(".")[2]
        }

        val data = """
            // ${title.decapitalize()}
            create(
                ${routes.joinToString(",\n                ")}
            )
        }"""

        file.readLines().forEach { line ->
            if (lastLine.trim().startsWith("import ") && line.trim() == "") {
                updatedFileData.append(imports).append("\n\n")
            } else if (lastLine == "            )" && line == "        }") {
                updatedFileData.append(data).append("\n")
            } else {
                updatedFileData.append(line).append("\n")
            }

            lastLine = line
        }

        file.writeText(updatedFileData.toString())
        echo("updated ${file.path}")
    }

    private fun updateSettingsGradle(lowered: String) {
        val file = File("./settings.gradle.kts")

        val updatedFileData = StringBuilder()

        val data = """include("$lowered")"""

        val lastLine = file.readLines().last()

        file.readLines().forEach { line ->
            updatedFileData.append(line).append("\n")
            if (line == lastLine) updatedFileData.append(data).append("\n")
        }

        file.writeText(updatedFileData.toString())
        echo("updated ${file.path}")
    }
}
