package com.hartwig.actin.clinical.feed.medication

import com.hartwig.actin.clinical.feed.FeedEntry
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.time.LocalDate

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class MedicationEntry : FeedEntry {
    abstract override fun subject(): String
    abstract fun codeText(): String
    abstract fun code5ATCCode(): String
    abstract fun code5ATCDisplay(): String
    abstract fun chemicalSubgroupDisplay(): String
    abstract fun pharmacologicalSubgroupDisplay(): String
    abstract fun therapeuticSubgroupDisplay(): String
    abstract fun anatomicalMainGroupDisplay(): String
    abstract fun dosageInstructionRouteDisplay(): String
    abstract fun dosageInstructionDoseQuantityUnit(): String
    abstract fun dosageInstructionDoseQuantityValue(): Double
    abstract fun dosageInstructionFrequencyUnit(): String
    abstract fun dosageInstructionFrequencyValue(): Double?
    abstract fun dosageInstructionMaxDosePerAdministration(): Double?
    abstract fun dosageInstructionPatientInstruction(): String
    abstract fun dosageInstructionAsNeededDisplay(): String
    abstract fun dosageInstructionPeriodBetweenDosagesUnit(): String
    abstract fun dosageInstructionPeriodBetweenDosagesValue(): Double?
    abstract fun dosageInstructionText(): String
    abstract fun status(): String
    abstract fun active(): Boolean?
    abstract fun dosageDoseValue(): String
    abstract fun dosageRateQuantityUnit(): String
    abstract fun dosageDoseUnitDisplayOriginal(): String
    abstract fun periodOfUseValuePeriodStart(): LocalDate
    abstract fun periodOfUseValuePeriodEnd(): LocalDate?
    abstract fun stopTypeDisplay(): String
}