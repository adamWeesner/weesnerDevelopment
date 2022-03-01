import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.maven

fun SourceSetContainer.sharedSources() {
    getByName("main").java.srcDirs("src/main/kotlin")
    getByName("test").java.srcDirs("src/test/kotlin")
    getByName("main").resources.srcDirs("resources")
    getByName("test").resources.srcDirs("testresources")
}

fun RepositoryHandler.sharedRepos() {
    maven(url = "https://jitpack.io")
    mavenCentral()
    jcenter()
}

fun JavaPluginExtension.javaSource() {
    sourceCompatibility = Jvm.javaVersion
}