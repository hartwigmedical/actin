package com.hartwig.actin.datamodel.molecular.immunology

import com.hartwig.actin.datamodel.molecular.evidence.Actionable
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence

data class HlaAllele(
    val gene: String,
    val alleleGroup: String,
    val hlaProtein: String,
    val tumorCopyNumber: Double?,
    val hasSomaticMutations: Boolean?,
    override val evidence: ClinicalEvidence,
    override val event: String
) : Actionable, Comparable<HlaAllele> {

    override fun compareTo(other: HlaAllele): Int {
        return Comparator.comparing(HlaAllele::gene)
            .thenComparing(HlaAllele::alleleGroup)
            .thenComparing(HlaAllele::hlaProtein)
            .compare(this, other)
    }
}