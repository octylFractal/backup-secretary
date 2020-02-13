package capnproto

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.setProperty
import org.gradle.kotlin.dsl.setValue
import java.io.File

/**
 * Compiles `.capnp` files to a target language.
 */
open class CapnProtoCompile : DefaultTask() {

    @InputDirectory
    val compilerLibrariesProperty = project.objects.directoryProperty()
        .convention(project.layout.capnprotoExecutables.map { it.dir("lib") })

    @get:Internal
    var compilerLibraries: Directory by compilerLibrariesProperty

    /**
     * Directories to pass to `capnp` as `-I` include directories.
     */
    @InputFiles
    val includes = project.objects.setProperty<Directory>()
        .convention(project.layout.capnprotoExecutables.map {
            setOf(it.dir("include"))
        })

    @InputFile
    val compilerExecutableProperty = project.objects.fileProperty()
        .convention(project.layout.capnprotoExecutables.map { it.file("bin/capnp") })

    @get:Internal
    var compilerExecutable: RegularFile by compilerExecutableProperty

    @Input
    val languageProperty = project.objects.property<String>()
        .convention("java")

    @get:Internal
    var language: String by languageProperty

    @OutputDirectory
    val outputDirectoryProperty = project.objects.directoryProperty()
        .convention(project.layout.buildDirectory.dir("compileCapnProto"))

    @get:Internal
    var outputDirectory: Directory by outputDirectoryProperty

    // we need info from SourceDirectorySet in particular
    // but people shouldn't set it, it's managed by CapnProtoPlugin
    @InputFiles
    @SkipWhenEmpty
    @PathSensitive(PathSensitivity.RELATIVE)
    lateinit var sourceFiles: SourceDirectorySet
        internal set

    @TaskAction
    fun compile() {
        for (tree in sourceFiles.srcDirTrees) {
            project.exec {
                environment("LD_LIBRARY_PATH", compilerLibraries.asFile.absolutePath)
                environment("PWD", workingDir)
                executable(compilerExecutable)
                args("compile")
                // Find the common root for the sources
                args("--src-prefix=${tree.dir.absolutePath}")
                // Don't let the system affect us
                args("--no-standard-import")
                // Instead, use -Is from the jars/etc.
                args(includes.get().map { "-I${it.asFile.absolutePath}" })
                // Resolve our language executables
                val lang = language
                val langPath: File = when {
                    "/" in lang -> project.file(lang)
                    else -> project.layout.capnprotoExecutables
                        .map { it.file("bin/capnpc-$language") }
                        .get()
                        .asFile
                }
                args("-o${langPath.absolutePath}:${outputDirectory.asFile.absolutePath}")
                args(project.fileTree(tree.dir).matching(tree.patterns).files.map { it.absolutePath })
            }
        }
    }
}
