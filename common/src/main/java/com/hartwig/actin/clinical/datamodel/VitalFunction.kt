package com.hartwig.actin.clinical.datamodel

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.time.LocalDate

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class VitalFunction {
    abstract fun date(): LocalDate
    abstract fun category(): VitalFunctionCategory
    abstract fun subcategory(): String
    abstract fun value(): Double
    abstract fun unit(): String
}
