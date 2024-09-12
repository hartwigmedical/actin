package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevel
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidence
import com.hartwig.serve.datamodel.EvidenceLevelDetails

object TreatmentEvidenceFunctions {

    data class TreatmentEvidenceContent(val treatment: String, val cancerTypesWithDate: String, val isResistant: Boolean)

    fun filterOnLabel(treatmentEvidenceSet: Set<TreatmentEvidence>, onLabel: Boolean): Set<TreatmentEvidence> {
        return treatmentEvidenceSet.filter { it.onLabel == onLabel }.toSet()
    }

    fun filterOutPreClinicalEvidence(treatmentEvidenceSet: Set<TreatmentEvidence>): Set<TreatmentEvidence> {
        return treatmentEvidenceSet.filter { it.evidenceLevel != EvidenceLevel.D || !isPreclinical(it) }.toSet()
    }

    private fun isPreclinical(evidence: TreatmentEvidence): Boolean {
        return evidence.evidenceLevelDetails == EvidenceLevelDetails.PRECLINICAL
    }

    fun groupTreatmentsIgnoringEvidenceLevel(treatmentEvidenceSet: Set<TreatmentEvidence>) =
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

    fun treatmentEvidenceToClinicalDetails(treatmentEvidenceList: List<TreatmentEvidence>): List<ClinicalDetails> {
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

    fun groupByTreatment(treatmentEvidence: List<TreatmentEvidence>) =
        treatmentEvidence.groupBy { it.treatment }

    fun generateEvidenceCellContents(evidenceList: List<TreatmentEvidence>): List<TreatmentEvidenceContent> {
        return groupByTreatment(evidenceList).map { (treatment, evidences) ->
            val cancerTypesWithYears = evidences
                .groupBy { it.applicableCancerType.cancerType }
                .map { (cancerType, evidenceGroup) ->
                    val years = evidenceGroup.map { it.date.year }.distinct().sorted()
                    "$cancerType (${years.joinToString(", ")})"
                }
                .joinToString(", ")
            val isResistant = evidences.any { it.direction.isResistant }
            TreatmentEvidenceContent(treatment, cancerTypesWithYears, isResistant)
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