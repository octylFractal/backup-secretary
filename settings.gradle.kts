rootProject.name = "backup-secretary"

include("config")
include("core")
include("cli")
include("util")
include("modules:ipc:common")
include("modules:ipc:client")
include("modules:source:local")
include("modules:chunker:byte-count")
include("modules:target:s3")
include("modules:target:local")
