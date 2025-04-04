package com.hartwig.actin.datamodel.molecular.driver

import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence

data class HomozygousDisruption(
    override val isReportable: Boolean,
    override val event: String,
    override val driverLikelihood: DriverLikelihood?,
    override val evidence: ClinicalEvidence,
    override val gene: String,
    override val geneRole: GeneRole,
    override val proteinEffect: ProteinEffect,
    override val isAssociatedWithDrugResistance: Boolean?
) : Driver, GeneAlteration, Comparable<HomozygousDisruption> {

    override fun compareTo(other: HomozygousDisruption): Int {
        return Comparator.comparing<HomozygousDisruption, HomozygousDisruption>({ it }, DriverComparator())
            .thenComparing({ it }, GeneAlterationComparator()).compare(this, other)
    }
}