package androidx.constraintlayout.coremp.platform

import org.jetbrains.skiko.currentNanoTime

internal actual object System {
    actual fun nanoTime(): Long = currentNanoTime()

    actual val err: PrintStream
        get() = object : PrintStream {
            override fun println(value: String) {
                println(value)
            }
        }
}
