package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence

interface Driver {
    val isReportable: Boolean
    val event: String
    val driverLikelihood: DriverLikelihood?
    val vaf: Double?
    val evidence: ActionableEvidence
}
