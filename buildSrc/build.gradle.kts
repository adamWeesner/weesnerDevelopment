plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

tasks.named("compileKotlin").get().dependsOn("generateProjectModules")

sourceSets {
    getByName("main").java.srcDirs("${project.buildDir}/generated/sources/generateProjectModules/main/kotlin")
}

tasks.register<ProjectModuleGeneratorTask>("generateProjectModules") {
    // replace this with your base package name
    basePackage = "com.weesnerdevelopment"

    input.set(layout.projectDirectory.file("../settings.gradle.kts"))

    val outputFile = layout
        .buildDirectory
        .file("generated/sources/$name/main/kotlin/${basePackage.replace(".", "/")}/ProjectGradleModule.kt")

    output.set(outputFile)
}

abstract class ProjectModuleGeneratorTask : DefaultTask() {
    @get:Input
    abstract var basePackage: String

    @get:InputFile
    abstract val input: RegularFileProperty

    @get:OutputFile
    abstract val output: RegularFileProperty

    @TaskAction
    fun generateEnum() {
        val inputFile = input.get().asFile
        val outputFile = output.get().asFile

        if (!outputFile.exists()) {
            println("output file didn't exist; creating...")
            outputFile.parentFile.mkdirs()
            outputFile.writeText("")
        }

        val gradleModules = inputFile.readLines().filter {
            !it.startsWith("//") && !it.startsWith("rootProject")
        }.mapNotNull {
            val filtered = it
                .replace("include", "")
                .replace(Regex("""["(),]"""), "")
                .trim()

            if (filtered.isBlank()) {
                null
            } else {
                val enumName = filtered
                    .split(":", "_", "-")
                    .joinToString("") { it.capitalize() }

                """/**
                 * Gradle Module - $filtered
                 */
                ${enumName}(project = "$filtered")"""
            }
        }

        val modulesEnum = """
            import org.gradle.kotlin.dsl.DependencyHandlerScope
            import org.gradle.kotlin.dsl.project
            
            /**
             * The projects gradle modules in a type safe way built from the settings.gradle.kts file. This meant to help with 
             * type-safety and allow for easier adding of gradle modules without needing to worry about typos ;)
             * 
             * There are many ways to use this, for your gradle module that for examples sake is named ":businessRules" you can do 
             * any of the following:
             * ```kotlin
             * implementation|api(project(ProjectGradleModule.BusinessRules.project))
             * ```
             * ```kotlin
             * implementation|api(ProjectGradleModule.BusinessRules)
             * ```
             * ```kotlin
             * addInternal|addExternal(ProjectGradleModule.BusinessRules)
             * ```
             * ```kotlin
             * isPrivate|isPublic(ProjectGradleModule.BusinessRules)
             * ```
             * ```kotlin
             * testImplementation|testApi(project(ProjectGradleModule.BusinessRules.project))
             * ```
             * ```kotlin
             * testImplementation|testApi(ProjectGradleModule.BusinessRules)
             * ```
             * ```kotlin
             * addTestInternal|addTestExternal(ProjectGradleModule.BusinessRules)
             * ```
             * ```kotlin
             * isTestPrivate|isTestPublic(ProjectGradleModule.BusinessRules)
             * ```
             * 
             * @param project The name of the gradle module, to be used for something like `implementation(project(":businessRules"))` 
             * is replaced with one of the helper functions noted above in the [ProjectGradleModule]!
             */
            enum class ProjectGradleModule(val project: String) {
                ${gradleModules.joinToString(",\n                ")}
            }
            
            
            /**
             * Special version of implementation that only takes the [module] and adds it as a 
             * `implementation(project({[module.project]}))`
             */
            fun DependencyHandlerScope.implementation(vararg module: ProjectGradleModule) {
                module.forEach { add("implementation", project(it.project)) }
            }
            
            /**
             * Same as [implementation] in different verbiage to note that this is a Gradle module internal project dependency.
             */
            fun DependencyHandlerScope.addInternal(vararg module: ProjectGradleModule) = implementation(*module)
            
            /**
             * Same as [implementation] in different verbiage to note that this is a private Gradle module project dependency.
             */
            fun DependencyHandlerScope.isPrivate(vararg module: ProjectGradleModule) = implementation(*module)
            
            /**
             * Special version of api that only takes the [module] and adds it as a
             * `api(project({[module.project]}))`
             */
            fun DependencyHandlerScope.api(vararg module: ProjectGradleModule) {
                module.forEach { add("api", project(it.project)) }
            }
            
            /**
             * Same as [api] in different verbiage to note that this is a Gradle module external project dependency.
             */
            fun DependencyHandlerScope.addExternal(vararg module: ProjectGradleModule) = api(*module)
            
            /**
             * Same as [api] in different verbiage to note that this is a public facing Gradle module project dependency.
             */
            fun DependencyHandlerScope.isPublic(vararg module: ProjectGradleModule) = api(*module)
            
            /**
             * Special version of testImplementation that only takes the [module] and adds it as a
             * `testImplementation(project({[module.project]}))`
             */
            fun DependencyHandlerScope.testImplementation(vararg module: ProjectGradleModule) {
                module.forEach { add("testImplementation", project(it.project)) }
            }
            
            /**
             * Same as [testImplementation] in different verbiage to note that this is a Gradle module internal testing project
             * dependency.
             */
            fun DependencyHandlerScope.addTestInternal(vararg module: ProjectGradleModule) = testImplementation(*module)
            
            /**
             * Same as [testImplementation] in different verbiage to note that this is a private Gradle module testing project
             * dependency.
             */
            fun DependencyHandlerScope.isTestPrivate(vararg module: ProjectGradleModule) = testImplementation(*module)
            
            /**
             * Special version of testApi that only takes the [module] and adds it as a
             * `testApi(project({[module.project]}))`
             */
            fun DependencyHandlerScope.testApi(vararg module: ProjectGradleModule) {
                module.forEach { add("testApi", project(it.project)) }
            }
            
            /**
             * Same as [testApi] in different verbiage to note that this is a Gradle module external test project dependency.
             */
            fun DependencyHandlerScope.addTestExternal(vararg module: ProjectGradleModule) = testApi(*module)
            
            /**
             * Same as [testApi] in different verbiage to note that this is a public facing Gradle module test project dependency.
             */
            fun DependencyHandlerScope.isTestPublic(vararg module: ProjectGradleModule) = testApi(*module)

        """.trimIndent()

        if (outputFile.readText() != modulesEnum) {
            println("Data seems to have changed, updating generated file...")
            outputFile.writeText(modulesEnum)
        }
        println("Generated project gradle modules enum at ${outputFile.path}")
    }
}
