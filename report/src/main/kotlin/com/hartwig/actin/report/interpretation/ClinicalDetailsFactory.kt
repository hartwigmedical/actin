package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevel
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidence

class ClinicalDetailsFactory(private val onLabel: Boolean) {

    fun create(evidence: ClinicalEvidence): Set<ClinicalDetails> {
        val treatmentEvidenceSet = filterOnLabel(evidence.treatmentEvidence)
        val groupedTreatments = TreatmentEvidenceFunctions.groupTreatmentsIgnoringEvidenceLevel(treatmentEvidenceSet)

        return groupedTreatments.flatMap { (_, treatmentEvidenceList) ->
            treatmentEvidenceToClinicalDetails(treatmentEvidenceList)
        }.toSet()
    }

    private fun filterOnLabel(treatmentEvidenceSet: Set<TreatmentEvidence>): Set<TreatmentEvidence> {
        return treatmentEvidenceSet.filter { it.onLabel == onLabel }.toSet()
    }

    private fun treatmentEvidenceToClinicalDetails(treatmentEvidenceList: List<TreatmentEvidence>): List<ClinicalDetails> {
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

    private fun extractVariants(treatmentEvidenceList: List<TreatmentEvidence>, isCategoryVariant: Boolean): List<TreatmentEvidence> =
        treatmentEvidenceList.filter { it.isCategoryVariant == isCategoryVariant }

    private fun findHighestEvidenceLevel(treatmentEvidenceList: List<TreatmentEvidence>): EvidenceLevel? =
        treatmentEvidenceList.minOfOrNull { it.evidenceLevel }

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
