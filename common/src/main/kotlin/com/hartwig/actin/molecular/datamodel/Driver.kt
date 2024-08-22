package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.molecular.datamodel.evidence.ClinicalEvidence

interface Driver {
    val isReportable: Boolean
    val event: String
    val driverLikelihood: DriverLikelihood?
    val evidence: ClinicalEvidence
}
