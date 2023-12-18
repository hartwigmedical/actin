package com.hartwig.actin.treatment.input.single

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class])
abstract class OneGeneOneInteger() {
    abstract fun geneName(): String
    abstract fun integer(): Int
}
