package androidx.constraintlayout.compose.platform.annotation

/**
 * Replace when androidx.annotation.IntRange support wasm
 */
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FIELD,
    AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.ANNOTATION_CLASS,
)
annotation class IntRange(
    val from: Long = Long.MIN_VALUE,
    val to: Long = Long.MAX_VALUE,
)
