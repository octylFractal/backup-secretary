package capnproto

abstract class CapnProtoExtension {
    abstract var executableVersion: String
    abstract var javaExecutableVersion: String
    abstract var runtimeVersion: String

    init {
        val buildNumber = "19"
        executableVersion = "0.7.0+build.$buildNumber"
        javaExecutableVersion = "0.1.5-4f514ad+build.$buildNumber"
        runtimeVersion = "0.1.4"
    }
}
