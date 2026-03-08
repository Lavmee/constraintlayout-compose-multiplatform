package tech.annexflow.constraintlayout.compose.platform

internal object BasicLog {
    fun e(tag: String, msg: String) {
        println("$tag: $msg")
    }

    fun d(tag: String, msg: String) {
        println("$tag: $msg")
    }

    fun w(tag: String, msg: String) {
        println("$tag: $msg")
    }
}

internal expect object Log {
    fun e(tag: String, msg: String)

    fun d(tag: String, msg: String)

    fun w(tag: String, msg: String)
}
