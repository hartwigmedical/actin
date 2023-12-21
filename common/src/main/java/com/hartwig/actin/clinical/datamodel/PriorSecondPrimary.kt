package com.hartwig.actin.clinical.datamodel

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class PriorSecondPrimary {
    abstract fun tumorLocation(): String
    abstract fun tumorSubLocation(): String
    abstract fun tumorType(): String
    abstract fun tumorSubType(): String
    abstract fun doids(): Set<String?>
    abstract fun diagnosedYear(): Int?
    abstract fun diagnosedMonth(): Int?
    abstract fun treatmentHistory(): String
    abstract fun lastTreatmentYear(): Int?
    abstract fun lastTreatmentMonth(): Int?
    abstract fun status(): TumorStatus
}
