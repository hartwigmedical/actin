package com.hartwig.actin.molecular.datamodel.driver

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class Fusion : Driver {
    abstract fun geneStart(): String
    abstract fun geneTranscriptStart(): String
    abstract fun fusedExonUp(): Int
    abstract fun geneEnd(): String
    abstract fun geneTranscriptEnd(): String
    abstract fun fusedExonDown(): Int
    abstract fun driverType(): FusionDriverType
    abstract fun proteinEffect(): ProteinEffect

    @JvmField
    abstract val isAssociatedWithDrugResistance: Boolean?
}
