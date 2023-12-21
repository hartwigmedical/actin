package com.hartwig.actin.clinical.datamodel

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class ECG {
    abstract fun hasSigAberrationLatestECG(): Boolean
    abstract fun aberrationDescription(): String?
    abstract fun qtcfMeasure(): ECGMeasure?
    abstract fun jtcMeasure(): ECGMeasure?
}
