package com.hartwig.actin.clinical.curation.config

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class MedicationDosageConfig : CurationConfig {
    abstract override fun input(): String
    override fun ignore(): Boolean {
        return false
    }

    abstract fun dosageMin(): Double?
    abstract fun dosageMax(): Double?
    abstract fun dosageUnit(): String?
    abstract fun frequency(): Double?
    abstract fun frequencyUnit(): String?
    abstract fun ifNeeded(): Boolean?
}