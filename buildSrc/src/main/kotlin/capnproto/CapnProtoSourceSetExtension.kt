package capnproto

import org.gradle.api.file.SourceDirectorySet

/**
 * Represents the Cap'n Proto sources on a [org.gradle.api.tasks.SourceSet].
 */
open class CapnProtoSourceSetExtension {

    lateinit var capnproto: SourceDirectorySet
        internal set

}
