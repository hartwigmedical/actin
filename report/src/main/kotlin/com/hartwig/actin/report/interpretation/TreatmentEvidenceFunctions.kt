package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevel
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidence

object TreatmentEvidenceFunctions {

    internal fun filterOnLabel(treatmentEvidenceSet: Set<TreatmentEvidence>, onLabel: Boolean): Set<TreatmentEvidence> {
        return treatmentEvidenceSet.filter { it.onLabel == onLabel }.toSet()
    }

    internal fun groupTreatmentsIgnoringEvidenceLevel(treatmentEvidenceSet: Set<TreatmentEvidence>) =
        treatmentEvidenceSet.groupBy {
            TreatmentEvidenceGroupingKey(
                it.treatment,
                it.onLabel,
                it.direction,
                it.isCategoryEvent,
                it.sourceEvent,
                it.applicableCancerType
            )
        }

    internal fun treatmentEvidenceToClinicalDetails(treatmentEvidenceList: List<TreatmentEvidence>): List<ClinicalDetails> {
        val categoryVariants = extractVariants(treatmentEvidenceList, true)
        val nonCategoryVariants = extractVariants(treatmentEvidenceList, false)
        val highestCategoryEvidenceLevel = findHighestEvidenceLevel(categoryVariants)
        val highestNonCategoryEvidenceLevel = findHighestEvidenceLevel(nonCategoryVariants)

        val nonCategoryDetails = generateClinicalDetails(nonCategoryVariants, highestNonCategoryEvidenceLevel)

        val categoryDetails = highestCategoryEvidenceLevel
            ?.takeIf { level -> highestNonCategoryEvidenceLevel == null || level < highestNonCategoryEvidenceLevel }
            ?.let { generateClinicalDetails(categoryVariants, it) }
            ?: emptyList()

        return nonCategoryDetails + categoryDetails
    }

    internal fun groupByTreatment(treatmentEvidence: List<TreatmentEvidence>) =
        treatmentEvidence.groupBy { it.treatment }

    internal fun generateEvidenceCellContents(evidenceList: List<TreatmentEvidence>): List<Triple<String, String, Boolean>> {
        return groupByTreatment(evidenceList).map { (treatment, evidences) ->
            val cancerTypesWithDate = evidences.joinToString(", ") { evidence ->
                "${evidence.applicableCancerType.cancerType} (${evidence.date.year})"
            }
            val isResistant = evidences.any { it.direction.isResistant }
            Triple(treatment, cancerTypesWithDate, isResistant)
        }
    }

    private fun findHighestEvidenceLevel(treatmentEvidenceList: List<TreatmentEvidence>): EvidenceLevel? =
        treatmentEvidenceList.minOfOrNull { it.evidenceLevel }

    private fun extractVariants(treatmentEvidenceList: List<TreatmentEvidence>, isCategoryEvent: Boolean): List<TreatmentEvidence> =
        treatmentEvidenceList.filter { it.isCategoryEvent == isCategoryEvent }

    private fun generateClinicalDetails(treatments: List<TreatmentEvidence>, level: EvidenceLevel?): List<ClinicalDetails> =
        level?.let {
            treatments
                .filter { it.evidenceLevel == level }
                .map { evidence ->
                    ClinicalDetails(
                        evidence,
                        level == EvidenceLevel.A,
                        level == EvidenceLevel.B,
                        level == EvidenceLevel.C,
                        level == EvidenceLevel.D
                    )
                }
        } ?: emptyList()
}