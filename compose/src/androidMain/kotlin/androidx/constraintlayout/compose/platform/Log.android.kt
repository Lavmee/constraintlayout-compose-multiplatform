package androidx.constraintlayout.compose.platform

internal actual object Log {
    actual fun e(tag: String, msg: String) {
        android.util.Log.e(tag, msg)
    }

    actual fun d(tag: String, msg: String) {
        android.util.Log.d(tag, msg)
    }

    actual fun w(tag: String, msg: String) {
        android.util.Log.w(tag, msg)
    }
}
