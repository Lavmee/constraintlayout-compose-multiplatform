package androidx.constraintlayout.coremp.platform

expect class WeakReference<T : Any>(referred: T) {
    fun clear()
    fun get(): T?
}
