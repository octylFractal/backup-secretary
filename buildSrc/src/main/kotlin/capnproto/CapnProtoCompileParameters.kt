package capnproto

import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.SetProperty
import org.gradle.workers.WorkParameters

interface CapnProtoCompileParameters : WorkParameters {
    val compilerLibraryPath: DirectoryProperty
    val compilerExecutable: RegularFileProperty

    val sourceFile: RegularFileProperty
    val sourcePrefix: DirectoryProperty
    val includes: SetProperty<Directory>
    val languagePath: RegularFileProperty
    val outputDirectory: DirectoryProperty
}
