package com.hartwig.actin.clinical.datamodel

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class ClinicalStatus {
    abstract fun who(): Int?
    abstract fun infectionStatus(): InfectionStatus?
    abstract fun ecg(): ECG?
    abstract fun lvef(): Double?
    abstract fun hasComplications(): Boolean?
}
