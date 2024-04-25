package androidx.constraintlayout.coremp.platform

internal actual object System {
    actual fun nanoTime(): Long = java.lang.System.nanoTime()

    actual val err: PrintStream
        get() = object : PrintStream {
            override fun println(value: String) {
                java.lang.System.err.println(value)
            }
        }
}
