package generator

import com.github.ajalt.clikt.output.TermUi.echo
import java.io.File

data class Builder(
    val title: String,
    val sharedFolder: String
) {
    private lateinit var baseDirectory: File
    private lateinit var entryInfo: EntryInfo

    private val titleTrimmed = title.split(" ").mapIndexed { index, item ->
        if (index == 0) item.toLowerCase()
        else item.capitalize()
    }.joinToString("")

    private val titleTrimmedCap = titleTrimmed.capitalize()

    init {
        createBaseDirectory()
        createGradleFile()

        SharedLibInfo(sharedFolder, baseDirectory) { className, data ->
            entryInfo = EntryInfo(baseDirectory, className, data)

            generateTable()
            generateService()
            generateRouter()
        }
        generateRoutes(titleTrimmedCap, entryInfo)
        updateSettingsGradle(titleTrimmed)
        updateBackendGradle(titleTrimmed)
        updateBuildSrc(titleTrimmed)
        updateDatabaseServer(titleTrimmed)
    }

    private fun String.buildFile(fileData: () -> String) {
        val file = File(this)
        val data = fileData()

        file.writeText(data)
        echo("+ ${file.path}")
    }

    private fun <T> String.toTableItem(
        string: T,
        stringNullable: T = string,
        integer: T = string,
        integerNullable: T = integer,
        double: T = integer,
        doubleNullable: T = double,
        listValue: () -> T? = { string },
        elseValue: () -> T
    ): T? = when {
        this == "String" -> string
        this == "String?" -> stringNullable
        this == "Int" -> integer
        this == "Int?" -> integerNullable
        this == "Double" -> double
        this == "Double?" -> doubleNullable
        this.matches(Regex("List<.*>\\??")) -> listValue()
        else -> elseValue()
    }

    private fun String.slimmed() = this
        .replace("$titleTrimmed/src/main/kotlin/", "")
        .replace("/", ".")
        .replace(".kt", "")

    private fun createBaseDirectory() {
        baseDirectory = File("$titleTrimmed/src/main/kotlin/$titleTrimmed")
        baseDirectory.mkdirs()
        echo("+ ${baseDirectory.path}")
    }

    private fun createGradleFile() = "$titleTrimmed/build.gradle.kts".buildFile {
        """
        import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

        plugins {
            id(Kotlin.jvm)
            id(Kotlin.kapt)
        }

        group = Base.group
        version = $titleTrimmedCap.version

        sourceSets { sharedSources() }
        repositories { sharedRepos() }
        java { javaSource() }
        tasks.withType<KotlinCompile>().all { kotlinOptions.jvmTarget = Jvm.version }

        dependencies {
            implementation(project(BusinessRules.project))
            implementation(Ktor.Server.core)
            implementation(Moshi.core)
            implementation(Exposed.core)
        }
        
        """.trimIndent()
    }

    private fun generateTable() = "${entryInfo.subDirectory.path}/${entryInfo.className}sTable.kt".buildFile {
        val tableInfo = entryInfo.data.map {
            val split = it.split(":")
            val name = split[0].trim()
            val typeName = split[1].trim()
            val type = typeName.toTableItem(
                "varchar(\"$name\", 255)",
                "varchar(\"$name\", 255).nullable()",
                "integer(\"$name\")",
                "integer(\"$name\").nullable()",
                "double(\"$name\")",
                "double(\"$name\").nullable()",
                {
                    val nullable = typeName.endsWith("?")
                    val named = if (name.endsWith("s")) name.dropLast(1) else name
                    val typeTrimmed = typeName
                        .replace(Regex("List<"), "")
                        .replace(Regex(">\\??"), "")

                    val typeLabeled: String? =
                        typeTrimmed.toTableItem(
                            "$named: $typeTrimmed",
                            elseValue = { "${typeTrimmed.decapitalize()}Id" }
                        )

                    val subEntryInfo = EntryInfo(
                        baseDirectory,
                        "${entryInfo.className}${name.capitalize()}",
                        listOf(typeLabeled!!, "itemId: ${entryInfo.className}")
                    )

                    generateRelationshipClass(subEntryInfo)
                    generateRelationshipTable(subEntryInfo, entryInfo.className)
                    generateRelationshipService(subEntryInfo, nullable)
                    null
                },
                { "reference(\"${typeName.decapitalize()}Id\", ${typeName}sTable.id)" }
            )
            if (type == null) null
            else "val $name = $type"
        }

        val packageName = "$titleTrimmed.${entryInfo.lowered}"
        val additionalImports =
            if (tableInfo.any { it?.contains("ImagesTable") == true }) "\n        import $titleTrimmed.image.ImagesTable"
            else ""
        val objectData = tableInfo.filterNotNull().joinToString("\n            ")

        """
        package $packageName

        import generics.IdTable$additionalImports

        object ${entryInfo.className}sTable : IdTable() {
            $objectData
        }

        """.trimIndent()
    }

    private fun generateService() = "${entryInfo.subDirectory.path}/${entryInfo.className}sService.kt".buildFile {
        val packageName = "$titleTrimmed.${entryInfo.lowered}"
        val extraServices = arrayListOf<String>()
        val extraImports = arrayListOf<String>()

        val tableInfo = entryInfo.data.map {
            val split = it.split(":")
            val name = split[0].trim()
            val type = split[1].trim()

            val typeLower = type.decapitalize()

            type.toTableItem(
                "row[table.$name]" to "this[table.$name] = item.$name",
                listValue = {
                    val service = "${entryInfo.className}${name.capitalize()}Service"
                    val serviceLower = service.decapitalize()

                    extraServices.add("private val $serviceLower: $service")
                    extraImports.add("import $titleTrimmed.${serviceLower.replace("Service", "")}.$service")
                    "$serviceLower.getFor(row[table.id])" to null
                },
                elseValue = {
                    extraServices.add("private val ${typeLower}sService: ${type}sService")
                    extraImports.add("import $titleTrimmed.$typeLower.${type}sService")
                    "${typeLower}sService.toItem(row)" to "this[table.$typeLower] = item.$typeLower.id!!"
                }
            )
        }

        """
        package $packageName

        import BaseService
        import org.jetbrains.exposed.sql.Join
        import org.jetbrains.exposed.sql.ResultRow
        import org.jetbrains.exposed.sql.statements.UpdateBuilder
        import shared.$sharedFolder.${entryInfo.className}${
            if (extraImports.isNotEmpty()) "\n        ${
                extraImports.joinToString("\n        ")
            }" else ""
        }
        
        class ${entryInfo.className}sService${
            if (extraServices.isEmpty()) "" else "(\n            ${extraServices.joinToString(",\n            ")}\n        )"
        } : BaseService<${entryInfo.className}sTable, ${entryInfo.className}>(
            ${entryInfo.className}sTable
        ) {
            override val ${entryInfo.className}sTable.connections: Join?
                get() = null
        
            override suspend fun toItem(row: ResultRow) = ${entryInfo.className}(
                row[table.id],
                ${tableInfo.filter { it?.first != null }.joinToString(",\n                ") { it!!.first }},
                row[table.dateCreated],
                row[table.dateUpdated]
            )
        
            override fun UpdateBuilder<Int>.toRow(item: ${entryInfo.className}) {
                ${tableInfo.filter { it?.second != null }.joinToString("\n                ") { it?.second!! }}
            }
        }
        
        """.trimIndent()
    }

    private fun generateRouter() = "${entryInfo.subDirectory.path}/${entryInfo.className}sRouter.kt".buildFile {
        """
        package $titleTrimmed.${entryInfo.lowered}

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
        
        """.trimIndent()
    }

    private fun generateRelationshipClass(entryInfo: EntryInfo) {
        val trimmedName = if (entryInfo.className.endsWith("s")) entryInfo.className.dropLast(1)
        else entryInfo.className

        "${entryInfo.subDirectory.path}/${trimmedName}.kt".buildFile {
            val tableInfo = entryInfo.data.map {
                val name = it.substringBefore(":")
                val typeName = it.substringAfter(":").trim()
                val type = typeName.toTableItem(typeName, elseValue = { "Int" })

                "val $name: $type"
            }

            """
            package $titleTrimmed.${entryInfo.lowered}
            
            import shared.base.GenericItem
            import shared.currentTimeMillis
            
            data class $trimmedName(
                override val id: Int?,
                ${tableInfo.joinToString(",\n                ")},
                override val dateCreated: Long = currentTimeMillis(),
                override val dateUpdated: Long = currentTimeMillis()
            ) : GenericItem
            
            """.trimIndent()
        }
    }

    private fun generateRelationshipTable(entryInfo: EntryInfo, baseLocation: String) =
        "${entryInfo.subDirectory.path}/${entryInfo.className}Table.kt".buildFile {
            val extraImports = mutableSetOf<String>()
            val tableInfo = entryInfo.data.map {
                val name = it.substringBefore(":").trim()
                val typeName = it.substringAfter(":").trim()

                val type = typeName.toTableItem(
                    "varchar(\"$name\", 255)",
                    "varchar(\"$name\", 255).nullable()",
                    "integer(\"$name\")",
                    "integer(\"$name\").nullable()",
                    "double(\"$name\")",
                    "double(\"$name\").nullable()",
                    elseValue = {
                        val item = if (typeName.endsWith("Id")) {
                            val type = typeName.dropLast(2)
                            extraImports.add("import $titleTrimmed.${type.decapitalize()}.${type.capitalize()}sTable")
                            type
                        } else {
                            typeName
                        }
                        "reference(\"${typeName.decapitalize()}\", ${item.capitalize()}sTable.id, ReferenceOption.CASCADE)"
                    }
                )
                "val $name = $type"
            }

            """
            package $titleTrimmed.${entryInfo.lowered}
            
            import $titleTrimmed.${baseLocation.decapitalize()}.${baseLocation.capitalize()}sTable
            import generics.IdTable
            import org.jetbrains.exposed.sql.ReferenceOption${
                if (extraImports.isNotEmpty()) "\n            ${extraImports.joinToString("\n            ")}" else ""
            }
            
            object ${entryInfo.className}Table : IdTable() {
                ${tableInfo.joinToString("\n                ")}
            }
            
            """.trimIndent()
        }

    private fun generateRelationshipService(entryInfo: EntryInfo, nullable: Boolean) {
        val trimmedName =
            if (entryInfo.className.endsWith("s")) entryInfo.className.dropLast(1) else entryInfo.className

        "${entryInfo.subDirectory.path}/${entryInfo.className}Service.kt".buildFile {
            val tableInfo = entryInfo.data.map {
                val name = it.substringBefore(":")
                "row[table.$name]," to "this[table.$name] = item.$name"
            }

            """
            package $titleTrimmed.${entryInfo.lowered}
            
            import BaseService
            import org.jetbrains.exposed.sql.Join
            import org.jetbrains.exposed.sql.ResultRow
            import org.jetbrains.exposed.sql.statements.UpdateBuilder${
                if (!nullable) "\n            import shared.base.InvalidAttributeException" else ""
            }
            
            class ${entryInfo.className}Service : BaseService<${entryInfo.className}Table, $trimmedName>(
                ${entryInfo.className}Table
            ) {
                override val ${entryInfo.className}Table.connections: Join?
                    get() = null
                    
                suspend fun getFor(id: Int) = getAll {
                    ${entryInfo.className}Table.itemId eq id
                }?.map { toItem(it) }${if (nullable) "" else " ?: throw InvalidAttributeException(\"${entryInfo.className}\")"}
            
                override suspend fun toItem(row: ResultRow) = $trimmedName(
                    row[table.id],
                    ${tableInfo.joinToString("\n                    ") { it.first }}
                    row[table.dateCreated],
                    row[table.dateUpdated]
                )
            
                override fun UpdateBuilder<Int>.toRow(item: $trimmedName) {
                    ${tableInfo.joinToString("\n                    ") { it.second }}
                }
            }
            
            """.trimIndent()
        }
    }

    private fun generateRoutes(title: String, entryInfo: EntryInfo) {
        val routers = arrayListOf<File>()
        val services = arrayListOf<File>()
        val tables = arrayListOf<File>()
        val names = routers.map { it.name.decapitalize().replace(".kt", "") }

        entryInfo.baseDirectory.listFiles()?.forEach {
            if (it.isDirectory) it.listFiles()?.forEach {
                if (it.name.endsWith("Router.kt")) routers.add(it)
                if (it.name.endsWith("Service.kt")) services.add(it)
                if (it.name.endsWith("Table.kt")) tables.add(it)
            }
        }

        "./backend/src/main/kotlin/routes/${title}Routes.kt".buildFile {
            """
            package com.weesnerdevelopment.routes
            
            ${routers.joinToString("\n") { "import ${it.path.slimmed()}" }}
            import io.ktor.auth.*
            import io.ktor.routing.*
            import org.kodein.di.generic.instance
            import org.kodein.di.ktor.kodein
            
            fun Routing.${title.decapitalize()}Routes() {
                ${names.joinToString("\n                ") { "val $it by kodein().instance<${it.capitalize()}>()" }}
            
                ${
                names.joinToString("\n\n                ") {
                    "${it}.apply {\n                    authenticate {\n                    setupRoutes()\n                    }\n                }"
                }
            }
            }
    
            """.trimIndent()
        }

        updateInjectionRouters(titleTrimmed, routers)
        updateInjectionServices(titleTrimmed, services)
        updatePaths(titleTrimmed, routers)
        updateDatabaseFactory(tables)
    }

    private fun updateInjectionRouters(baseDir: String, routers: List<File>) {
        val file = File("./backend/src/main/kotlin/injection/Routers.kt")
        var lastLine = ""
        val updatedFileData = StringBuilder()

        val imports = routers.joinToString("\n") {
            "import ${it.path.slimmed()}"
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

        val imports = services.joinToString("\n") { "import ${it.path.slimmed()}" }

        val bindings = services.joinToString("\n") {
            val trimmedName = it.name.replace(".kt", "")
            "    bind<$trimmedName>() with singleton { $trimmedName() }"
        }

        file.readLines().forEach { line ->
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
            it.path.slimmed().replace("Router", "").split(".")[1].decapitalize()
        }

        val data =
            """
            /**
             * The available paths at [basePath]/value.
             */
             object ${baseDir.capitalize()} : Path() {
                val basePath = "$baseDir/"
                val all = "${"\${basePath}"}all"
            ${routes.joinToString("\n") { "        val ${it}s = \"${"\${basePath}"}${it}s\"" }}
            }
            
            """.trimIndent()

        file.readLines().forEach { line ->
            if (lastLine.trim().startsWith("import ") && line.trim() == "")
                updatedFileData.append("import Path.${baseDir.capitalize()}.basePath\n\n")
            else if (lastLine == "    }" && line == "}") updatedFileData.append("$data\n}")
            else updatedFileData.append("$line\n")

            lastLine = line
        }

        file.writeText(updatedFileData.toString())
        echo("updated ${file.path}")
    }

    private fun updateDatabaseFactory(tables: List<File>) {
        val file = File("./backend/src/main/kotlin/service/DatabaseFactory.kt")
        var lastLine = ""
        val updatedFileData = StringBuilder()

        val imports = tables.joinToString("\n") { "import ${it.path.slimmed()}" }
        val routes = tables.map { it.path.slimmed().split(".")[2] }

        val data = """            // ${title.decapitalize()}
            create(${routes.joinToString(",\n                ")})
        }"""

        file.readLines().forEach { line ->
            if (lastLine.trim().startsWith("import ") && line.trim() == "") updatedFileData.append("$imports\n\n")
            else if (lastLine == "            )" && line == "        }") updatedFileData.append(data).append("\n")
            else updatedFileData.append(line).append("\n")

            lastLine = line
        }

        file.writeText(updatedFileData.toString())
        echo("updated ${file.path}")
    }

    private fun updateSettingsGradle(lowered: String) {
        val file = File("./settings.gradle.kts")
        val lastLine = file.readLines().last()
        val updatedFileData = StringBuilder()
        val data = """include("$lowered")"""

        file.readLines().forEach { line ->
            updatedFileData.append(line).append("\n")
            if (line == lastLine) updatedFileData.append(data).append("\n")
        }

        file.writeText(updatedFileData.toString())
        echo("updated ${file.path}")
    }

    private fun updateBackendGradle(lowered: String) {
        val file = File("./backend/build.gradle.kts")
        var lastLine = ""
        val updatedFileData = StringBuilder()
        val data = """    implementation(project(${lowered.capitalize()}.project))"""

        file.readLines().forEach { line ->
            if (lastLine.startsWith("    implementation(project") && !line.startsWith("    implementation(project")) {
                updatedFileData.append(data).append("\n")
            }
            updatedFileData.append(line).append("\n")
            lastLine = line
        }

        file.writeText(updatedFileData.toString())
        echo("updated ${file.path}")
    }

    private fun updateBuildSrc(lowered: String) {
        val name = lowered.capitalize()
        val file = File("./buildSrc/src/main/kotlin/$name.kt")

        val data =
            """
            object $name {
                const val version = "1.0.0"
            
                const val project = ":$lowered"
            }
            
            """.trimIndent()

        file.writeText(data)
        echo("+ ${file.path}")
    }

    private fun updateDatabaseServer(lowered: String) {
        val file = File("./backend/src/main/kotlin/service/DatabaseServer.kt")
        var lastLine = ""
        val updatedFileData = StringBuilder()
        val data = """            ${lowered}Routes()"""

        file.readLines().forEach { line ->
            if (lastLine.contains("Routes()") && line == "        }") updatedFileData.append("${data}\n        }\n")
            else updatedFileData.append(line).append("\n")

            lastLine = line
        }

        file.writeText(updatedFileData.toString())
        echo("updated ${file.path}")
    }
}
