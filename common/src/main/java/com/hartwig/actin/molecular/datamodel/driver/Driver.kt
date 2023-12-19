package com.hartwig.actin.molecular.datamodel.driver

import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence

interface Driver {
    @JvmField
    val isReportable: Boolean
    fun event(): String
    fun driverLikelihood(): DriverLikelihood?
    fun evidence(): ActionableEvidence
}
