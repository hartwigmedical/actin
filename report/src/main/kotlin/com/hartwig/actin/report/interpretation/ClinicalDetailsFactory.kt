package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.evidence.ApplicableCancerType
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceDirection

data class TreatmentEvidenceGroupingKey(
    val treatment: String,
    val onLabel: Boolean,
    val direction: EvidenceDirection,
    val isCategoryVariant: Boolean?,
    val sourceEvent: String,
    val applicableCancerType: ApplicableCancerType
)

class ClinicalDetailsFactory(private val onLabel: Boolean?) {

    fun create(evidence: ClinicalEvidence): Set<ClinicalDetails> {
        val evidenceSet = evidence.treatmentEvidence
        val labelFilteredEvidence = onLabel?.let { TreatmentEvidenceFunctions.filterOnLabel(evidenceSet, it) }
            ?: evidenceSet
        val groupedTreatments = TreatmentEvidenceFunctions.groupTreatmentsIgnoringEvidenceLevel(labelFilteredEvidence)

        return groupedTreatments.flatMap { (_, treatmentEvidenceList) ->
            TreatmentEvidenceFunctions.treatmentEvidenceToClinicalDetails(treatmentEvidenceList)
        }.toSet()
    }
}
