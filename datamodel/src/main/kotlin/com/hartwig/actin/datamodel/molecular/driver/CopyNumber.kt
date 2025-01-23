package com.hartwig.actin.datamodel.molecular.driver

import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.sort.driver.DriverComparator
import com.hartwig.actin.datamodel.molecular.sort.driver.GeneAlterationComparator

data class CopyNumber(
    val canonicalImpact: TranscriptCopyNumberImpact,
    val otherImpacts: Set<TranscriptCopyNumberImpact>,
    override val isReportable: Boolean,
    override val event: String,
    override val driverLikelihood: DriverLikelihood?,
    override val evidence: ClinicalEvidence,
    override val gene: String,
    override val geneRole: GeneRole,
    override val proteinEffect: ProteinEffect,
    override val isAssociatedWithDrugResistance: Boolean?
) : Driver, GeneAlteration, Comparable<CopyNumber> {

    override fun compareTo(other: CopyNumber): Int {
        return Comparator.comparing<CopyNumber, CopyNumber>({ it }, DriverComparator())
            .thenComparing({ it }, GeneAlterationComparator())
            .compare(this, other)
    }
}