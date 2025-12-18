package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencedHlaAllele
import com.hartwig.actin.datamodel.molecular.immunology.HlaAllele
import com.hartwig.actin.datamodel.molecular.immunology.MolecularImmunology
import com.hartwig.actin.molecular.util.ExtractionUtil

private val HLA_REGEX = Regex(pattern = """^(?:HLA-)?(?<gene>[A-Z0-9]+)\*(?<alleleGroup>\d{2,}):(?<hlaProtein>\d{2,})(?::\d{2,})*$""")

class PanelImmunologyAnnotator {

    fun annotate(hlaAlleles: Set<SequencedHlaAllele>): MolecularImmunology? {
        val extractedAlleles = hlaAlleles.map(::toMolecularHlaAllele).toSet()
        return extractedAlleles.takeIf { it.isNotEmpty() }?.let { MolecularImmunology(isReliable = true, hlaAlleles = it) }
    }

    private fun toMolecularHlaAllele(hlaAllele: SequencedHlaAllele): HlaAllele {
        val match = HLA_REGEX.matchEntire(hlaAllele.name)
            ?: throw IllegalArgumentException(
                "Can't extract HLA gene, alleleGroup and hlaProtein from '${hlaAllele.name}' (example: A*02:01)"
            )
        val gene = match.groups["gene"]!!.value
        val alleleGroup = match.groups["alleleGroup"]!!.value
        val hlaProtein = match.groups["hlaProtein"]!!.value
        val normalizedAllele = hlaAllele.name.removePrefix("HLA-")

        return HlaAllele(
            gene = "HLA-$gene",
            alleleGroup = alleleGroup,
            hlaProtein = hlaProtein,
            tumorCopyNumber = hlaAllele.tumorCopyNumber,
            hasSomaticMutations = hlaAllele.hasSomaticMutations,
            evidence = ExtractionUtil.noEvidence(),
            event = "HLA-$normalizedAllele"
        )
    }
}