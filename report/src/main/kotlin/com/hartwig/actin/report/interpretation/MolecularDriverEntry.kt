package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class MolecularDriverEntry {
    abstract fun driverType(): String
    abstract fun driver(): String
    abstract fun driverLikelihood(): DriverLikelihood?
    abstract fun actinTrials(): Set<String?>
    abstract fun externalTrials(): Set<String?>
    abstract fun bestResponsiveEvidence(): String?
    abstract fun bestResistanceEvidence(): String?
}