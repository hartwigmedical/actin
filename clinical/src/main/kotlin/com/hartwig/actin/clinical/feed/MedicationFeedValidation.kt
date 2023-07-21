package com.hartwig.actin.clinical.feed

import com.hartwig.actin.clinical.AtcModel
import com.hartwig.actin.clinical.datamodel.AtcLevel
import com.hartwig.actin.clinical.feed.medication.MedicationEntry

class MedicationFeedValidation(private val atcModel: AtcModel) {

    fun validate(medicationEntry: MedicationEntry): List<String> {
        val code5ATCCode = medicationEntry.code5ATCCode
        val atcClassification = atcModel.resolve(code5ATCCode)
        if (atcClassification != null) {
            return checkLevel(medicationEntry.anatomicalMainGroupDisplay, atcClassification.anatomicalMainGroup(), "anatomical display") +
                    checkLevel(
                        medicationEntry.therapeuticSubgroupDisplay,
                        atcClassification.therapeuticSubGroup(),
                        "therapeutic subgroup"
                    ) +
                    checkLevel(
                        medicationEntry.pharmacologicalSubgroupDisplay,
                        atcClassification.pharmacologicalSubGroup(),
                        "pharmacological subgroup"
                    ) +
                    checkLevel(medicationEntry.chemicalSubgroupDisplay, atcClassification.chemicalSubGroup(), "chemical subgroup") +
                    checkLevel(medicationEntry.code5ATCDisplay, atcClassification.chemicalSubstance(), "chemical substance")
        }
        return emptyList()
    }

    private fun checkLevel(levelFeedValue: String, levelTreeValue: AtcLevel, levelName: String): List<String> {
        if (!levelsEqual(levelFeedValue, levelTreeValue)) {
            return listOf(errorMessage(levelFeedValue, levelName, levelTreeValue.name()))
        }
        return emptyList()
    }

    private fun levelsEqual(feedValue: String, atcValue: AtcLevel) = atcValue.name().equals(feedValue, ignoreCase = true)

    private fun errorMessage(feedValue: String, levelName: String, atcValue: String?) =
        "ATC $levelName not equal between feed value of '$feedValue' and ATC tree value of '$atcValue'"
}