package com.hartwig.actin.molecular.orange

import com.hartwig.actin.datamodel.molecular.immunology.HlaAllele
import com.hartwig.actin.datamodel.molecular.immunology.MolecularImmunology
import com.hartwig.actin.molecular.util.ExtractionUtil
import com.hartwig.hmftools.datamodel.hla.LilacAllele
import com.hartwig.hmftools.datamodel.hla.LilacRecord
import com.hartwig.hmftools.datamodel.orange.OrangeRecord

object ImmunologyExtraction {

    const val LILAC_QC_PASS: String = "PASS"
    private val HLA_REGEX = Regex(pattern = """^(?<gene>[A-Z]+)\*(?<alleleGroup>\d{2}):(?<hlaProtein>\d{2,3})$""")

    fun extract(record: OrangeRecord): MolecularImmunology {
        val lilac = record.lilac()
        return MolecularImmunology(isReliable = isQCPass(lilac), hlaAlleles = toHlaAlleles(lilac?.alleles() ?: emptyList()))
    }

    private fun isQCPass(lilac: LilacRecord?): Boolean {
        return lilac?.qc() == LILAC_QC_PASS
    }

    private fun toHlaAlleles(alleles: List<LilacAllele>): Set<HlaAllele> {
        return alleles.map { allele ->
            val hasSomaticVariants = allele.somaticMissense() > 0 || allele.somaticNonsenseOrFrameshift() > 0 ||
                    allele.somaticSplice() > 0 || allele.somaticInframeIndel() > 0

            val match = HLA_REGEX.matchEntire(allele.allele())
                ?: throw IllegalStateException("Can't extract HLA gene, alleleGroup and hlaProtein from ${allele.allele()}")
            val gene = match.groups["gene"]!!.value
            val alleleGroup = match.groups["alleleGroup"]!!.value
            val hlaProtein = match.groups["hlaProtein"]!!.value

            HlaAllele(
                gene = "HLA-$gene",
                alleleGroup = alleleGroup,
                hlaProtein = hlaProtein,
                tumorCopyNumber = allele.tumorCopyNumber(),
                hasSomaticMutations = hasSomaticVariants,
                evidence = ExtractionUtil.noEvidence(),
                event = DriverEventFactory.immunologyEvent(allele)
            )
        }.toSet()
    }
}