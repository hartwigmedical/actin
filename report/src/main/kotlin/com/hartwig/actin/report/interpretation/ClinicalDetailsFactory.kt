package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.evidence.ApplicableCancerType
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceDirection

internal data class TreatmentEvidenceGroupingKey(
    val treatment: String,
    val onLabel: Boolean,
    val direction: EvidenceDirection,
    val isCategoryVariant: Boolean?,
    val sourceEvent: String,
    val applicableCancerType: ApplicableCancerType
)

class ClinicalDetailsFactory(private val onLabel: Boolean) {

    fun create(evidence: ClinicalEvidence): Set<ClinicalDetails> {
        val treatmentEvidenceSet = TreatmentEvidenceFunctions.filterOnLabel(evidence.treatmentEvidence, onLabel)
        val groupedTreatments = TreatmentEvidenceFunctions.groupTreatmentsIgnoringEvidenceLevel(treatmentEvidenceSet)

        return groupedTreatments.flatMap { (_, treatmentEvidenceList) ->
            TreatmentEvidenceFunctions.treatmentEvidenceToClinicalDetails(treatmentEvidenceList)
        }.toSet()
    }
}
