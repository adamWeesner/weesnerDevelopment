package generator

import generator.classes.GenFile
import generator.classes.GeneratorFile
import generator.classes.Template
import java.io.File
import java.util.*

internal fun String.capitalize() =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

internal fun String.decapitalize() =
    replaceFirstChar { it.lowercase(Locale.getDefault()) }

data class Builder(
    val title: String,
    val sharedFolder: String
) {
    private lateinit var baseDirectory: GenFile
    private lateinit var entryInfo: EntryInfo

    private val titleTrimmed = title.split(" ").mapIndexed { index, item ->
        if (index == 0) item.lowercase()
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
            generateRoutes(titleTrimmedCap)
        }
        updateSettingsGradle(titleTrimmed)
        updateBackendGradle(titleTrimmed)
        updateBuildSrc(titleTrimmed)
        updateDatabaseServer(titleTrimmed)
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
        baseDirectory = GeneratorFile.create("$titleTrimmed/src/main/kotlin/$titleTrimmed")
    }

    private fun createGradleFile() {
        GeneratorFile.create("$titleTrimmed/build.gradle.kts").save {
            Template("buildGradle").update { listOf("#1" to titleTrimmedCap) }
        }
    }

    private fun generateTable() {
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
            if (tableInfo.any { it?.contains("ImagesTable") == true }) "\nimport $titleTrimmed.image.ImagesTable"
            else ""
        val objectData = tableInfo.filterNotNull().joinToString("\n    ")

        GeneratorFile.create("${entryInfo.subDirectory.path}/${entryInfo.className}sTable.kt").save {
            Template("table").update {
                listOf(
                    "#package" to packageName,
                    "#imports" to additionalImports,
                    "#className" to entryInfo.className,
                    "#data" to objectData
                )
            }
        }
    }

    private fun generateService() {
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

        GeneratorFile.create("${entryInfo.subDirectory.path}/${entryInfo.className}sService.kt").save {
            Template("service").update {
                listOf(
                    "#package" to packageName,
                    "#sharedFolder" to sharedFolder,
                    "#class" to entryInfo.className,
                    "#imports" to if (extraImports.isNotEmpty()) "\n${extraImports.joinToString("\n")}" else "",
                    "variables" to if (extraServices.isEmpty()) "" else "(\n    ${extraServices.joinToString(",\n    ")}\n)",
                    "#toItemExtras" to tableInfo.filter { it?.first != null }
                        .joinToString(",\n        ") { it!!.first },
                    "#toRowExtras" to tableInfo.filter { it?.second != null }
                        .joinToString("\n        ") { it?.second!! }
                )
            }
        }
    }

    private fun generateRouter() {
        GeneratorFile.create("${entryInfo.subDirectory.path}/${entryInfo.className}sRouter.kt").save {
            Template("router").update {
                listOf(
                    "#title" to titleTrimmed,
                    "#classNameLower" to entryInfo.lowered,
                    "#sharedFolder" to sharedFolder,
                    "#className" to entryInfo.className
                )
            }
        }
    }

    private fun generateRelationshipClass(entryInfo: EntryInfo) {
        val trimmedName =
            if (entryInfo.className.endsWith("s")) entryInfo.className.dropLast(1)
            else entryInfo.className

        val tableInfo = entryInfo.data.map {
            val name = it.substringBefore(":")
            val typeName = it.substringAfter(":").trim()
            val type = typeName.toTableItem(typeName, elseValue = { "Int" })

            "val $name: $type"
        }

        GeneratorFile.create("${entryInfo.subDirectory.path}/${trimmedName}.kt").save {
            Template("relationshipClass").update {
                listOf(
                    "#package" to "$titleTrimmed.${entryInfo.lowered}",
                    "#name" to trimmedName,
                    "#variables" to tableInfo.joinToString(",\n    ")
                )
            }
        }
    }

    private fun generateRelationshipTable(entryInfo: EntryInfo, baseLocation: String) {
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
        val packageName = "$titleTrimmed.${entryInfo.lowered}"
        val tableImport = "$titleTrimmed.${baseLocation.decapitalize()}.${baseLocation.capitalize()}"
        val imports = if (extraImports.isNotEmpty()) "\n${extraImports.joinToString("\n")}" else ""
        val tableName = entryInfo.className
        val tableData = tableInfo.joinToString("\n    ")

        GeneratorFile.create("${entryInfo.subDirectory.path}/${entryInfo.className}Table.kt").save {
            Template("relationshipTable").update {
                listOf(
                    "#package" to packageName,
                    "#tableImport" to tableImport,
                    "#imports" to imports,
                    "#tableName" to tableName,
                    "#data" to tableData
                )
            }
        }
    }

    private fun generateRelationshipService(entryInfo: EntryInfo, nullable: Boolean) {
        val trimmedName =
            if (entryInfo.className.endsWith("s")) entryInfo.className.dropLast(1) else entryInfo.className

        val tableInfo = entryInfo.data.map {
            val name = it.substringBefore(":")
            "row[table.$name]," to "this[table.$name] = item.$name"
        }
        val extraImports = if (!nullable) "\nimport com.weesnerdevelopment.shared.base.InvalidAttributeException" else ""

        GeneratorFile.create("${entryInfo.subDirectory.path}/${entryInfo.className}Service.kt").save {
            Template("relationshipService").update {
                listOf(
                    "#package" to "$titleTrimmed.${entryInfo.lowered}",
                    "#imports" to extraImports,
                    "#classLower" to entryInfo.lowered,
                    "#class" to entryInfo.className,
                    "#name" to trimmedName,
                    "#nullableItem" to if (nullable) "" else " ?: throw InvalidAttributeException(\"#class\")",
                    "#toItemExtras" to tableInfo.joinToString("\n        ") { it.first },
                    "#toRowExtras" to tableInfo.joinToString("\n        ") { it.second }
                )
            }
        }
    }

    private fun generateRoutes(title: String) {
        val routers = arrayListOf<File>()
        val services = arrayListOf<File>()
        val tables = arrayListOf<File>()
        val names = routers.map { it.name.decapitalize().replace(".kt", "") }

        File(entryInfo.baseDirectory.path).listFiles()?.forEach { file ->
            if (file.isDirectory) file.listFiles()?.forEach {
                if (it.name.endsWith("Router.kt")) routers.add(it)
                if (it.name.endsWith("Service.kt")) services.add(it)
                if (it.name.endsWith("Table.kt")) tables.add(it)
            }
        }

        val imports = routers.joinToString("\n") {
            "import ${it.path.slimmed()}"
        }
        val instances = names.joinToString("\n    ") {
            "val $it by kodein().instance<${it.capitalize()}>()"
        }
        val routes = names.joinToString("\n\n    ") {
            "${it}.apply {\n        authenticate {\n            setupRoutes()\n        }\n        }"
        }

        GeneratorFile.create("./backend/src/main/kotlin/routes/${title}Routes.kt").save {
            Template("routes").update {
                listOf(
                    "#routeImports" to imports,
                    "#title" to title.decapitalize(),
                    "#instances" to instances,
                    "#routes" to routes
                )
            }
        }

        updateInjectionRouters(titleTrimmed, routers)
        updateInjectionServices(titleTrimmed, services)
        updatePaths(titleTrimmed, routers)
        updateDatabaseFactory(tables)
        createTests(routers)
    }

    private fun updateInjectionRouters(baseDir: String, routers: List<File>) {
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

        GeneratorFile.open("./backend/src/main/kotlin/injection/Routers.kt").update { lines ->
            lines.forEach { line ->
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
            updatedFileData.toString()
        }
    }

    private fun updateInjectionServices(baseDir: String, services: List<File>) {
        var lastLine = ""
        val updatedFileData = StringBuilder()

        val imports = services.joinToString("\n") { "import ${it.path.slimmed()}" }

        val bindings = services.joinToString("\n") {
            val trimmedName = it.name.replace(".kt", "")
            "    bind<$trimmedName>() with singleton { $trimmedName() }"
        }

        GeneratorFile.open("./backend/src/main/kotlin/injection/Services.kt").update { lines ->
            lines.forEach { line ->
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
            updatedFileData.toString()
        }
    }

    private fun updatePaths(baseDir: String, routers: List<File>) {
        var lastLine = ""
        val updatedFileData = StringBuilder()

        val routes = routers.map {
            it.path.slimmed().replace("Router", "").split(".")[1].decapitalize()
        }

        val data = Template("path").update {
            listOf(
                "#name" to baseDir.capitalize(),
                "#basePath" to baseDir,
                "#variables" to routes.joinToString("\n") { "   val ${it}s = \${basePath}${it}s\"" }
            )
        }

        GeneratorFile.open("./businessRules/src/main/kotlin/Paths.kt").update { lines ->
            lines.forEach { line ->
                if (lastLine.trim().startsWith("import ") && line.trim() == "")
                    updatedFileData.append("import Path.${baseDir.capitalize()}.basePath\n\n")
                else if (lastLine == "    }" && line == "}") updatedFileData.append("$data\n}")
                else updatedFileData.append("$line\n")

                lastLine = line
            }
            updatedFileData.toString()
        }
    }

    private fun updateDatabaseFactory(tables: List<File>) {
        var lastLine = ""
        val updatedFileData = StringBuilder()

        val imports = tables.joinToString("\n") { "import ${it.path.slimmed()}" }
        val routes = tables.map { it.path.slimmed().split(".")[2] }

        val data = """            // ${title.decapitalize()}
            create(
                ${routes.joinToString(",\n                ")}
            )
        }"""

        GeneratorFile.open("./backend/src/main/kotlin/service/DatabaseFactory.kt").update {
            it.forEach { line ->
                if (lastLine.trim().startsWith("import ") && line.trim() == "") updatedFileData.append("$imports\n\n")
                else if (lastLine == "            )" && line == "        }") updatedFileData.append(data).append("\n")
                else updatedFileData.append(line).append("\n")

                lastLine = line
            }
            updatedFileData.toString()
        }
    }

    private fun updateSettingsGradle(lowered: String) {
        val file = GeneratorFile.open("./settings.gradle.kts")
        val lastLine = file.data.split("\n").last { it.isNotEmpty() }
        val updatedFileData = StringBuilder()
        val data = """include("$lowered")"""

        file.update {
            it.forEach { line ->
                updatedFileData.append(line).append("\n")
                if (line == lastLine) updatedFileData.append(data).append("\n")
            }

            updatedFileData.toString()
        }
    }

    private fun updateBackendGradle(lowered: String) {
        var lastLine = ""
        val updatedFileData = StringBuilder()
        val data = """    implementation(project(${lowered.capitalize()}.project))"""

        GeneratorFile.open("./backend/build.gradle.kts").update {
            it.forEach { line ->
                if (lastLine.startsWith("    implementation(project") && !line.startsWith("    implementation(project")) {
                    updatedFileData.append(data).append("\n")
                }
                updatedFileData.append(line).append("\n")
                lastLine = line
            }
            updatedFileData.toString()
        }
    }

    private fun updateBuildSrc(lowered: String) {
        GeneratorFile.open("./buildSrc/src/main/kotlin/${lowered.capitalize()}.kt").update {
            Template("buildSrc").update {
                listOf(
                    "#nameLower" to lowered,
                    "#name" to lowered.capitalize()
                )
            }
        }
    }

    private fun updateDatabaseServer(lowered: String) {
        var lastLine = ""
        val updatedFileData = StringBuilder()
        val data = """            ${lowered}Routes()"""

        GeneratorFile.open("./backend/src/main/kotlin/service/DatabaseServer.kt").update {
            it.forEach { line ->
                if (lastLine.contains("Routes()") && line == "        }") updatedFileData.append("${data}\n        }\n")
                else updatedFileData.append(line).append("\n")

                lastLine = line
            }

            updatedFileData.toString()
        }
    }

    private fun createTests(routers: List<File>) {
        val dir = GeneratorFile.create("./backend/src/test/kotlin/$titleTrimmed")

        val routes = routers.map {
            it.path.slimmed().replace("Router", "").split(".")[1]
        }

        routes.forEach {
            val item = it.capitalize()
            val itemLower = it.decapitalize()
            val itemSingle = if (item.endsWith("s")) item.dropLast(1) else item
            val itemVal = itemSingle.decapitalize()

            GeneratorFile.create("${dir.path}/${itemSingle}Tests.kt").save {
                Template("routeTests").update {
                    listOf(
                        "#titleCap" to titleTrimmedCap,
                        "#title" to titleTrimmed,
                        "#sharedFolder" to sharedFolder,
                        "#itemLower" to itemLower,
                        "#itemVal" to itemVal,
                        "#item" to itemSingle
                    )
                }
            }
        }
    }
}
