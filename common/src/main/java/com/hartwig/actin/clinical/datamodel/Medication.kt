package com.hartwig.actin.clinical.datamodel

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.time.LocalDate

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class Medication {
    abstract fun name(): String
    abstract fun status(): MedicationStatus?
    abstract fun administrationRoute(): String?
    abstract fun dosage(): Dosage
    abstract fun startDate(): LocalDate?
    abstract fun stopDate(): LocalDate?
    abstract fun cypInteractions(): List<CypInteraction?>
    abstract fun qtProlongatingRisk(): QTProlongatingRisk
    abstract fun atc(): AtcClassification?

    @JvmField
    abstract val isSelfCare: Boolean

    @JvmField
    abstract val isTrialMedication: Boolean
    fun allLevels(): Set<AtcLevel> {
        return if (atc() == null) emptySet() else atc()!!.allLevels()
    }
}
