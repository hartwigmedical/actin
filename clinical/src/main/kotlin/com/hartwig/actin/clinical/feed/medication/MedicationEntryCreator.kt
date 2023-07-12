package com.hartwig.actin.clinical.feed.medication

import com.google.common.annotations.VisibleForTesting
import com.hartwig.actin.clinical.AtcModel
import com.hartwig.actin.clinical.datamodel.AtcLevel
import com.hartwig.actin.clinical.feed.FeedEntryCreator
import com.hartwig.actin.clinical.feed.FeedLine
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class MedicationEntryCreator(private val atcModel: AtcModel) : FeedEntryCreator<MedicationEntry> {

    override fun fromLine(line: FeedLine): MedicationEntry {
        return MedicationEntry(
            subject = line.trimmed("subject"),
            codeText = line.string("code_text"),
            code5ATCCode = line.string("code5_ATC_code"),
            code5ATCDisplay = line.string("code5_ATC_display"),
            chemicalSubgroupDisplay = line.string("chemical_subgroup_display"),
            pharmacologicalSubgroupDisplay = line.string("pharmacological_subgroup_display"),
            therapeuticSubgroupDisplay = line.string("therapeutic_subgroup_display"),
            anatomicalMainGroupDisplay = line.string("anatomical_main_group_display"),
            dosageInstructionRouteDisplay = line.string("dosageInstruction_route_display"),
            dosageInstructionDoseQuantityUnit = line.string("dosageInstruction_doseQuantity_unit"),
            dosageInstructionDoseQuantityValue = line.number("dosageInstruction_doseQuantity_value"),
            dosageInstructionFrequencyUnit = line.string("dosageInstruction_frequency_unit"),
            dosageInstructionFrequencyValue = line.optionalNumber("dosageInstruction_frequency_value"),
            dosageInstructionMaxDosePerAdministration = line.optionalNumber("dosageInstruction_maxDosePerAdministration"),
            dosageInstructionPatientInstruction = line.string("dosageInstruction_patientInstruction"),
            dosageInstructionAsNeededDisplay = line.string("dosageInstruction_asNeeded_display"),
            dosageInstructionPeriodBetweenDosagesUnit = line.string("dosageInstruction_period_between_dosages_unit"),
            dosageInstructionPeriodBetweenDosagesValue = line.optionalNumber("dosageInstruction_period_between_dosages_value"),
            dosageInstructionText = line.string("dosageInstruction_text"),
            status = line.string("status"),
            active = isActive(line.string("active")),
            dosageDoseValue = line.string("dosage_dose_value"),
            dosageRateQuantityUnit = line.string("dosage_rateQuantity_unit"),
            dosageDoseUnitDisplayOriginal = line.string("dosage_dose_unit_display_original"),
            periodOfUseValuePeriodStart = line.date("periodOfUse_valuePeriod_start"),
            periodOfUseValuePeriodEnd = line.optionalDate("periodOfUse_valuePeriod_end"),
            stopTypeDisplay = line.string("stopType_display")
        )
    }

    override fun isValid(line: FeedLine): Boolean {
        val code5ATCCode = line.string("code5_ATC_code")
        val atcClassification = atcModel.resolve(code5ATCCode)
        if (atcClassification != null) {
            val code5ATCDisplay = line.string("code5_ATC_display")
            val chemicalSubgroupDisplay = line.string("chemical_subgroup_display")
            val pharmacologicalSubgroupDisplay = line.string("pharmacological_subgroup_display")
            val therapeuticSubgroupDisplay = line.string("therapeutic_subgroup_display")
            val anatomicalMainGroupDisplay = line.string("anatomical_main_group_display")
            val errors = checkLevel(anatomicalMainGroupDisplay, atcClassification.anatomicalMainGroup(), "anatomical display") +
                    checkLevel(therapeuticSubgroupDisplay, atcClassification.therapeuticSubGroup(), "therapeutic subgroup") +
                    checkLevel(pharmacologicalSubgroupDisplay, atcClassification.pharmacologicalSubGroup(), "pharmacological subgroup") +
                    checkLevel(chemicalSubgroupDisplay, atcClassification.chemicalSubGroup(), "chemical subgroup") +
                    checkLevel(code5ATCDisplay, atcClassification.chemicalSubstance(), "chemical substance")
            if (errors.isNotEmpty()) {
                errors.forEach { LOGGER.error(it) }
                return false
            }
        }
        return true
    }

    private fun checkLevel(anatomicalMainGroupDisplay: String, atcLevel: AtcLevel, levelName: String): List<String> {
        if (!levelsEqual(anatomicalMainGroupDisplay, atcLevel)) {
            return listOf(errorMessage(anatomicalMainGroupDisplay, levelName, atcLevel.name()))
        }
        return emptyList()
    }

    private fun levelsEqual(feedValue: String, atcValue: AtcLevel) = atcValue.name().equals(feedValue, ignoreCase = true)

    private fun errorMessage(feedValue: String, levelName: String, atcValue: String?) =
        "ATC $levelName not equal between feed value of '$feedValue' and ATC tree value of '$atcValue'"

    companion object {
        val LOGGER: Logger = LogManager.getLogger(MedicationEntryCreator::class.java)

        @VisibleForTesting
        fun isActive(activeField: String): Boolean? {
            if (activeField.equals("stopped", ignoreCase = true)) {
                return false
            } else if (activeField == "active") {
                return true
            }
            return null
        }
    }
}