import org.gradle.api.JavaVersion
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSetContainer

fun SourceSetContainer.sharedSources() {
    getByName("main").java.srcDirs("src/main")
    getByName("test").java.srcDirs("src/test")
    getByName("main").resources.srcDirs("resources")
    getByName("test").resources.srcDirs("testresources")
}

fun RepositoryHandler.sharedRepos() {
    mavenCentral()
    jcenter()
}

fun JavaPluginExtension.javaSource() {
    sourceCompatibility = JavaVersion.VERSION_1_8
}