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
                it.isCategoryVariant,
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

    private fun findHighestEvidenceLevel(treatmentEvidenceList: List<TreatmentEvidence>): EvidenceLevel? =
        treatmentEvidenceList.minOfOrNull { it.evidenceLevel }

    private fun extractVariants(treatmentEvidenceList: List<TreatmentEvidence>, isCategoryVariant: Boolean): List<TreatmentEvidence> =
        treatmentEvidenceList.filter { it.isCategoryVariant == isCategoryVariant }

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