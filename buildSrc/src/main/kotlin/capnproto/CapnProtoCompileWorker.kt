package capnproto

import org.gradle.process.ExecOperations
import org.gradle.workers.WorkAction
import javax.inject.Inject

abstract class CapnProtoCompileWorker @Inject constructor(
    private val execOperations: ExecOperations
) : WorkAction<CapnProtoCompileParameters> {
    override fun execute() {
        with(parameters) {
            execOperations.exec {
                environment("LD_LIBRARY_PATH", compilerLibraryPath.get().asFile.absolutePath)
                environment("PWD", workingDir)
                executable(compilerExecutable)
                args("compile")
                // Find the common root for the sources
                args("--src-prefix=${sourcePrefix.get().asFile.absolutePath}")
                // Don't let the system affect us
                args("--no-standard-import")
                // Instead, use -Is from the jars/etc.
                args(includes.get().map { "-I${it.asFile.absolutePath}" })
                // Resolve our language executables
                args("-o${languagePath.get().asFile.absolutePath}:${outputDirectory.get().asFile.absolutePath}")
                args(sourceFile.get().asFile.absolutePath)
            }
        }
    }
}
