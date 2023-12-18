package com.hartwig.actin.treatment.input.single

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class OneIntegerManyStrings() {
    abstract fun integer(): Int
    abstract fun strings(): List<String?>
}
