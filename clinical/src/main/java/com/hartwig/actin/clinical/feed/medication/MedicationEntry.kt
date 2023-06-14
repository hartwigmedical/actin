package com.hartwig.actin.clinical.feed.medication

import com.hartwig.actin.clinical.feed.FeedEntry
import java.time.LocalDate

data class MedicationEntry(
    override val subject: String,
    val codeText: String,
    val code5ATCCode: String,
    val code5ATCDisplay: String,
    val chemicalSubgroupDisplay: String,
    val pharmacologicalSubgroupDisplay: String,
    val therapeuticSubgroupDisplay: String,
    val anatomicalMainGroupDisplay: String,
    val dosageInstructionRouteDisplay: String,
    val dosageInstructionDoseQuantityUnit: String,
    val dosageInstructionDoseQuantityValue: Double,
    val dosageInstructionFrequencyUnit: String,
    val dosageInstructionFrequencyValue: Double?,
    val dosageInstructionMaxDosePerAdministration: Double?,
    val dosageInstructionPatientInstruction: String,
    val dosageInstructionAsNeededDisplay: String,
    val dosageInstructionPeriodBetweenDosagesUnit: String,
    val dosageInstructionPeriodBetweenDosagesValue: Double?,
    val dosageInstructionText: String,
    val status: String,
    val active: Boolean?,
    val dosageDoseValue: String,
    val dosageRateQuantityUnit: String,
    val dosageDoseUnitDisplayOriginal: String,
    val periodOfUseValuePeriodStart: LocalDate,
    val periodOfUseValuePeriodEnd: LocalDate?,
    val stopTypeDisplay: String
) : FeedEntry