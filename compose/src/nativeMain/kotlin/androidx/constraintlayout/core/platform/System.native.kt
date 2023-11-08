package androidx.constraintlayout.core.platform

internal actual object System {
    actual fun nanoTime(): Long = kotlin.system.getTimeNanos()

    actual val err: PrintStream
        get() = object : PrintStream {
            override fun println(value: String) {
                println(value)
            }
        }
}
