package androidx.constraintlayout.coremp.ext

internal fun DoubleArray.binarySearch(element: Double?, fromIndex: Int = 0, toIndex: Int = size) =
    this.toList().binarySearch(element, fromIndex, toIndex)
