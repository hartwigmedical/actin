package com.hartwig.actin.molecular.datamodel.wgs.driver

import com.hartwig.actin.molecular.datamodel.Driver
import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.sort.driver.VirusComparator

data class Virus(
    val name: String,
    val type: VirusType,
    val isReliable: Boolean,
    val integrations: Int,
    override val isReportable: Boolean,
    override val event: String,
    override val driverLikelihood: DriverLikelihood?,
    override val evidence: ActionableEvidence
) : Driver, Comparable<Virus> {

    override fun compareTo(other: Virus): Int {
        return VirusComparator().compare(this, other)
    }
}
