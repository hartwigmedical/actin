package com.hartwig.actin.molecular.datamodel.orange.driver

import com.hartwig.actin.molecular.datamodel.Driver
import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.evidence.ClinicalEvidence
import com.hartwig.actin.molecular.sort.driver.VirusComparator

data class Virus(
    val name: String,
    val type: VirusType,
    val isReliable: Boolean,
    val integrations: Int,
    override val isReportable: Boolean,
    override val event: String,
    override val driverLikelihood: DriverLikelihood?,
    override val evidence: ClinicalEvidence
) : Driver, Comparable<Virus> {

    override fun compareTo(other: Virus): Int {
        return VirusComparator().compare(this, other)
    }
}
