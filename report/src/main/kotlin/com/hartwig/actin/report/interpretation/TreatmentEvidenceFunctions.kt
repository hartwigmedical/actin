package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.evidence.ApplicableCancerType
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceDirection
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidence

object TreatmentEvidenceFunctions {

    data class TreatmentEvidenceGroupingKey(
        val treatment: String,
        val onLabel: Boolean,
        val direction: EvidenceDirection,
        val isCategoryVariant: Boolean?,
        val sourceEvent: String,
        val applicableCancerType: ApplicableCancerType
    )

    fun groupTreatmentsIgnoringEvidenceLevel(treatmentEvidenceSet: Set<TreatmentEvidence>) =
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

    fun mapTreatmentEvidencesToLevel(clinicalDetails: List<ClinicalDetails>) =
        listOf(
            clinicalDetails.filter { it.levelA }.map { it.treatmentEvidence },
            clinicalDetails.filter { it.levelB }.map { it.treatmentEvidence },
            clinicalDetails.filter { it.levelC }.map { it.treatmentEvidence },
            clinicalDetails.filter { it.levelD }.map { it.treatmentEvidence }
        )
}