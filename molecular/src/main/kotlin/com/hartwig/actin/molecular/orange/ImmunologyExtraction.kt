package com.hartwig.actin.molecular.orange

import com.hartwig.actin.datamodel.molecular.immunology.HlaAllele
import com.hartwig.actin.datamodel.molecular.immunology.MolecularImmunology
import com.hartwig.hmftools.datamodel.hla.LilacAllele
import com.hartwig.hmftools.datamodel.hla.LilacRecord
import com.hartwig.hmftools.datamodel.orange.OrangeRecord

object ImmunologyExtraction {

    const val LILAC_QC_PASS: String = "PASS"

    fun extract(record: OrangeRecord): MolecularImmunology {
        val lilac = record.lilac()
        return MolecularImmunology(isReliable = isQCPass(lilac), hlaAlleles = toHlaAlleles(lilac.alleles()))
    }

    private fun isQCPass(lilac: LilacRecord): Boolean {
        return lilac.qc() == LILAC_QC_PASS
    }

    private fun toHlaAlleles(alleles: List<LilacAllele>): Set<HlaAllele> {
        return alleles.map { allele ->
            val hasSomaticVariants = allele.somaticMissense() > 0 || allele.somaticNonsenseOrFrameshift() > 0 ||
                    allele.somaticSplice() > 0 || allele.somaticInframeIndel() > 0
            HlaAllele(
                name = allele.allele(),
                tumorCopyNumber = allele.tumorCopyNumber(),
                hasSomaticMutations = hasSomaticVariants
            )
        }.toSet()
    }
}