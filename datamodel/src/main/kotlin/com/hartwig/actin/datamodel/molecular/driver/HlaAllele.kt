package com.hartwig.actin.datamodel.molecular.driver

import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence

data class HlaAllele(
    val name: String,
    val tumorCopyNumber: Double,
    val hasSomaticMutations: Boolean,
    override val isReportable: Boolean,
    override val driverLikelihood: DriverLikelihood?,
    override val evidence: ClinicalEvidence,
    override val event: String
) : Driver, Comparable<HlaAllele> {

    override fun compareTo(other: HlaAllele): Int {
        return name.compareTo(other.name)
    }
}