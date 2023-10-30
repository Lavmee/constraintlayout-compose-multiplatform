package androidx.constraintlayout.compose.core.platform

expect class WeakReference<T : Any>(referred: T) {
    fun clear()
    fun get(): T?
}
