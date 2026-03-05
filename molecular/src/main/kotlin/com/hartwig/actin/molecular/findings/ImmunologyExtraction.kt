package com.hartwig.actin.molecular.findings

import com.hartwig.actin.datamodel.molecular.immunology.HlaAllele
import com.hartwig.actin.datamodel.molecular.immunology.MolecularImmunology
import com.hartwig.actin.molecular.orange.DriverEventFactory
import com.hartwig.actin.molecular.util.ExtractionUtil
import com.hartwig.hmftools.finding.datamodel.FindingList
import com.hartwig.hmftools.finding.datamodel.FindingsStatus

object ImmunologyExtraction {

    fun extract(hlaAlleles: FindingList<com.hartwig.hmftools.finding.datamodel.HlaAllele>): MolecularImmunology {
        return MolecularImmunology(isReliable = hlaAlleles.status == FindingsStatus.OK, hlaAlleles = toHlaAlleles(hlaAlleles.findings()))
    }

    private fun toHlaAlleles(alleles: List<com.hartwig.hmftools.finding.datamodel.HlaAllele>): Set<HlaAllele> {
        return alleles.map { allele ->
            HlaAllele(
                gene = allele.gene(),
                alleleGroup = allele.alleleGroup(),
                hlaProtein = allele.hlaProtein(),
                tumorCopyNumber = allele.tumorCopyNumber(),
                hasSomaticMutations = allele.hasSomaticVariants(),
                evidence = ExtractionUtil.noEvidence(),
                event = DriverEventFactory.event(allele)
            )
        }.toSet()
    }
}