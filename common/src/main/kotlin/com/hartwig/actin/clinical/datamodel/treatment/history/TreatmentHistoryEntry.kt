package com.hartwig.actin.clinical.datamodel.treatment.history

import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType

private const val DELIMITER = ";"

data class TreatmentHistoryEntry(
    val treatments: Set<Treatment>,
    val startYear: Int? = null,
    val startMonth: Int? = null,
    val intents: Set<Intent>? = null,
    val isTrial: Boolean = false,
    val trialAcronym: String? = null,
    val treatmentHistoryDetails: TreatmentHistoryDetails? = null
) {

    fun allTreatments(): Set<Treatment> {
        val switchToTreatments = treatmentHistoryDetails?.switchToTreatments?.map(TreatmentStage::treatment)?.toSet() ?: emptySet()
        return treatments + setOfNotNull(treatmentHistoryDetails?.maintenanceTreatment?.treatment) + switchToTreatments
    }

    fun treatmentName(): String {
        return treatmentStringUsingFunction(allTreatments(), Treatment::name)
    }

    fun categories(): Set<TreatmentCategory> {
        return allTreatments().flatMap(Treatment::categories).toSet()
    }

    fun isOfType(typeToFind: TreatmentType): Boolean? {
        return matchesTypeFromSet(setOf(typeToFind))
    }

    fun matchesTypeFromSet(types: Set<TreatmentType>): Boolean? {
        return if (hasTypeConfigured()) isTypeFromCollection(types) else null
    }

    fun hasTypeConfigured(): Boolean {
        return allTreatments().none { it.types().isEmpty() }
    }

    private fun isTypeFromCollection(types: Set<TreatmentType>): Boolean {
        return allTreatments().flatMap(Treatment::types).any(types::contains)
    }

    fun treatmentDisplay(): String {
        val treatmentNames = treatments.map(Treatment::display).map(String::lowercase).toSet()
        val chemoradiationTherapyNames = setOf("chemotherapy", "radiotherapy")
        if (treatmentNames.containsAll(chemoradiationTherapyNames)) {
            val remainingTreatments = treatments.filter { !chemoradiationTherapyNames.contains(it.display().lowercase()) }
            if (remainingTreatments.isEmpty()) {
                return "Chemoradiation"
            } else if (remainingTreatments.size == 1) {
                val remainingTreatment = remainingTreatments.first()
                return if (remainingTreatment.categories().contains(TreatmentCategory.CHEMOTHERAPY)) {
                    "Chemoradiation (with ${remainingTreatment.display()})"
                } else {
                    "Chemoradiation and ${remainingTreatment.display()}"
                }
            }
        }
        return treatmentStringUsingFunction(treatments, Treatment::display)
    }

    private fun treatmentStringUsingFunction(treatments: Set<Treatment>, treatmentField: (Treatment) -> String): String {
        return treatments.map(treatmentField).sorted().distinct().joinToString(DELIMITER)
            .ifEmpty { treatmentCategoryDisplay(treatments) }
    }

    private fun treatmentCategoryDisplay(treatments: Set<Treatment>): String {
        return treatments.flatMap { it.categories().map(TreatmentCategory::display) }
            .distinct()
            .joinToString(DELIMITER)
    }
}
