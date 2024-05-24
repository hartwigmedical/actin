package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence

interface Fusion: Driver {
    val geneStart: String
    val geneEnd: String
    override val isReportable: Boolean
    override val event: String
    override val driverLikelihood: DriverLikelihood?
    override val evidence: ActionableEvidence
}