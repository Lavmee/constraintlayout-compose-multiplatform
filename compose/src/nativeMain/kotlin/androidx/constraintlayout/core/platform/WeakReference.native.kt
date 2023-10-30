package androidx.constraintlayout.core.platform

import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
actual typealias WeakReference<T> = kotlin.native.ref.WeakReference<T>
