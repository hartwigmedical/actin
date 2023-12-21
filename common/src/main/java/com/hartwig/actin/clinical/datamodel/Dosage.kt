package com.hartwig.actin.clinical.datamodel

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class Dosage {
    abstract fun dosageMin(): Double?
    abstract fun dosageMax(): Double?
    abstract fun dosageUnit(): String?
    abstract fun frequency(): Double?
    abstract fun frequencyUnit(): String?
    abstract fun periodBetweenValue(): Double?
    abstract fun periodBetweenUnit(): String?
    abstract fun ifNeeded(): Boolean?
}
