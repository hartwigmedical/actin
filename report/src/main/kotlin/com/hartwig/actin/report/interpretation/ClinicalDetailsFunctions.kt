package com.hartwig.actin.report.interpretation

object ClinicalDetailsFunctions {

    fun groupBySourceEvent(clinicalDetailsSet: Set<ClinicalDetails>) =
        clinicalDetailsSet.groupBy { it.treatmentEvidence.sourceEvent }

    fun mapTreatmentEvidencesToLevel(clinicalDetails: List<ClinicalDetails>) =
        listOf(
            clinicalDetails.filter { it.levelA }.map { it.treatmentEvidence },
            clinicalDetails.filter { it.levelB }.map { it.treatmentEvidence },
            clinicalDetails.filter { it.levelC }.map { it.treatmentEvidence },
            clinicalDetails.filter { it.levelD }.map { it.treatmentEvidence }
        )
}