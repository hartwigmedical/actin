package com.hartwig.actin.molecular.interpreted

import com.hartwig.actin.molecular.datamodel.driver.Driver
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence

interface InterpretedFusion: Driver {
    val geneStart: String
    val geneEnd: String
    override val isReportable: Boolean
    override val event: String
    override val driverLikelihood: DriverLikelihood?
    override val evidence: ActionableEvidence
}