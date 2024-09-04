package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidence

data class ClinicalDetails(
    val treatmentEvidence: TreatmentEvidence,
    val levelA: Boolean,
    val levelB: Boolean,
    val levelC: Boolean,
    val levelD: Boolean
)
