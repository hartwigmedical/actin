package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.molecular.datamodel.characteristics.MolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers
import com.hartwig.actin.molecular.datamodel.immunology.MolecularImmunology
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.time.LocalDate

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class MolecularRecord {
    abstract fun patientId(): String
    abstract fun sampleId(): String
    abstract fun type(): ExperimentType
    abstract fun refGenomeVersion(): RefGenomeVersion
    abstract fun date(): LocalDate?
    abstract fun evidenceSource(): String
    abstract fun externalTrialSource(): String
    abstract fun containsTumorCells(): Boolean
    abstract fun hasSufficientQualityAndPurity(): Boolean
    abstract fun hasSufficientQuality(): Boolean
    abstract fun characteristics(): MolecularCharacteristics
    abstract fun drivers(): MolecularDrivers
    abstract fun immunology(): MolecularImmunology
    abstract fun pharmaco(): Set<PharmacoEntry?>
}
