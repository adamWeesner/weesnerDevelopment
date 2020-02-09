import org.gradle.api.JavaVersion
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSetContainer

fun SourceSetContainer.sharedSources() {
    mainSrc()
    testSrc()
    mainResources()
    testResources()
}

fun SourceSetContainer.mainSrc() = getByName("main").java.srcDirs("src")
fun SourceSetContainer.testSrc() = getByName("test").java.srcDirs("test")
fun SourceSetContainer.mainResources() = getByName("main").resources.srcDirs("resources")
fun SourceSetContainer.testResources() = getByName("test").resources.srcDirs("testresources")

fun RepositoryHandler.sharedRepos() {
    mavenCentral()
    jcenter()
}

fun JavaPluginExtension.javaSource() {
    sourceCompatibility = JavaVersion.VERSION_1_8
}