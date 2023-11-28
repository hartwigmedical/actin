package com.hartwig.actin.molecular.orange.interpretation

import com.google.common.collect.Sets
import com.hartwig.actin.molecular.datamodel.immunology.HlaAllele
import com.hartwig.actin.molecular.datamodel.immunology.ImmutableHlaAllele
import com.hartwig.actin.molecular.datamodel.immunology.ImmutableMolecularImmunology
import com.hartwig.actin.molecular.datamodel.immunology.MolecularImmunology
import com.hartwig.hmftools.datamodel.hla.LilacAllele
import com.hartwig.hmftools.datamodel.hla.LilacRecord
import com.hartwig.hmftools.datamodel.orange.OrangeRecord

internal object ImmunologyExtraction {

    const val LILAC_QC_PASS: String = "PASS"

    fun extract(record: OrangeRecord): MolecularImmunology {
        val lilac = record.lilac()
        return ImmutableMolecularImmunology.builder().isReliable(isQCPass(lilac)).hlaAlleles(toHlaAlleles(lilac.alleles())).build()
    }

    private fun isQCPass(lilac: LilacRecord): Boolean {
        return lilac.qc() == LILAC_QC_PASS
    }

    private fun toHlaAlleles(alleles: List<LilacAllele>): MutableSet<HlaAllele> {
        val hlaAlleles: MutableSet<HlaAllele> = Sets.newHashSet()
        for (allele in alleles) {
            val hasSomaticVariants =
                allele.somaticMissense() > 0 || allele.somaticNonsenseOrFrameshift() > 0 ||
                        allele.somaticSplice() > 0 || allele.somaticInframeIndel() > 0

            hlaAlleles.add(
                ImmutableHlaAllele.builder()
                    .name(allele.allele())
                    .tumorCopyNumber(allele.tumorCopyNumber())
                    .hasSomaticMutations(hasSomaticVariants)
                    .build()
            )
        }
        return hlaAlleles
    }
}
