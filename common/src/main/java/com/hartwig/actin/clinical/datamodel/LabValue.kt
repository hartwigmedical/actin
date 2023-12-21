package com.hartwig.actin.clinical.datamodel

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.time.LocalDate

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class LabValue {
    abstract fun date(): LocalDate
    abstract fun code(): String
    abstract fun name(): String
    abstract fun comparator(): String
    abstract fun value(): Double
    abstract fun unit(): LabUnit
    abstract fun refLimitLow(): Double?
    abstract fun refLimitUp(): Double?

    @JvmField
    abstract val isOutsideRef: Boolean?
}
