package com.hartwig.actin.datamodel.molecular.driver

import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence

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
        return Comparator.comparing<Virus, Virus>({ it }, DriverComparator())
            .thenComparing({ it.type.toString() }, String::compareTo)
            .thenComparing(Virus::name)
            .compare(this, other)
    }
}