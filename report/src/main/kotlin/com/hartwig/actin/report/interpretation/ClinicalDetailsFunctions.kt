package com.hartwig.actin.report.interpretation

object ClinicalDetailsFunctions {

    fun groupBySourceEvent(clinicalDetailsSet: Set<ClinicalDetails>) =
        clinicalDetailsSet.groupBy { it.treatmentEvidence.sourceEvent }
}