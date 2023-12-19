package com.hartwig.actin.molecular.datamodel.characteristics

import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class MolecularCharacteristics {
    abstract fun purity(): Double?
    abstract fun ploidy(): Double?
    abstract fun predictedTumorOrigin(): PredictedTumorOrigin?

    @kotlin.jvm.JvmField
    abstract val isMicrosatelliteUnstable: Boolean?
    abstract fun microsatelliteEvidence(): ActionableEvidence?
    abstract fun homologousRepairScore(): Double?
    abstract val isHomologousRepairDeficient: Boolean?
    abstract fun homologousRepairEvidence(): ActionableEvidence?
    abstract fun tumorMutationalBurden(): Double?
    abstract fun hasHighTumorMutationalBurden(): Boolean?
    abstract fun tumorMutationalBurdenEvidence(): ActionableEvidence?
    abstract fun tumorMutationalLoad(): Int?
    abstract fun hasHighTumorMutationalLoad(): Boolean?
    abstract fun tumorMutationalLoadEvidence(): ActionableEvidence?
}
