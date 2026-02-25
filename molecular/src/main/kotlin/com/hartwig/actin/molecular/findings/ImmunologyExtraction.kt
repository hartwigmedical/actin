package com.hartwig.actin.molecular.findings

import com.hartwig.actin.datamodel.molecular.immunology.HlaAllele
import com.hartwig.actin.datamodel.molecular.immunology.MolecularImmunology
import com.hartwig.actin.molecular.util.ExtractionUtil
import com.hartwig.hmftools.datamodel.finding.FindingList
import com.hartwig.hmftools.datamodel.finding.FindingsStatus

object ImmunologyExtraction {

    fun extract(hlaAlleles: FindingList<com.hartwig.hmftools.datamodel.finding.HlaAllele>): MolecularImmunology {
        return MolecularImmunology(isReliable = hlaAlleles.status == FindingsStatus.OK, hlaAlleles = toHlaAlleles(hlaAlleles.findings()))
    }

    private fun toHlaAlleles(alleles: List<com.hartwig.hmftools.datamodel.finding.HlaAllele>): Set<HlaAllele> {
        return alleles.map { allele ->
            HlaAllele(
                gene = allele.gene(),
                alleleGroup = allele.alleleGroup(),
                hlaProtein = allele.hlaProtein(),
                tumorCopyNumber = allele.tumorCopyNumber(),
                hasSomaticMutations = allele.hasSomaticVariants(),
                evidence = ExtractionUtil.noEvidence(),
                event = allele.event()
            )
        }.toSet()
    }
}