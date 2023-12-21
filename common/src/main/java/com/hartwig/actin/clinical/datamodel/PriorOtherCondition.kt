package com.hartwig.actin.clinical.datamodel

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class PriorOtherCondition {
    abstract fun name(): String
    abstract fun year(): Int?
    abstract fun month(): Int?
    abstract fun doids(): Set<String?>
    abstract fun category(): String

    @JvmField
    abstract val isContraindicationForTherapy: Boolean
}
