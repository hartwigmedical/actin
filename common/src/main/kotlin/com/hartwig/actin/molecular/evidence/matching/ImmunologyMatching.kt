package com.hartwig.actin.molecular.evidence.matching

import com.hartwig.actin.datamodel.molecular.immunology.HlaAllele
import com.hartwig.serve.datamodel.molecular.immuno.ActionableHLA

object ImmunologyMatching {

    private val HLA_REGEX = Regex(pattern = """^(?<gene>[A-Z]+)\*(?<alleleGroup>\d{2}):(?<hlaProtein>\d{2})$""")

    fun isMatch(actionableHla: ActionableHLA, hlaAllele: HlaAllele): Boolean {
        val match = HLA_REGEX.matchEntire(hlaAllele.name)
            ?: throw IllegalStateException("Can't extract HLA gene, alleleGroup and hlaProtein from ${hlaAllele.name}")
        val gene = match.groups["gene"]!!.value
        val alleleGroup = match.groups["alleleGroup"]!!.value
        val hlaProtein = match.groups["hlaProtein"]!!.value

        val geneMatch = actionableHla.gene() == "HLA-$gene"
        val alleleGroupMatch = actionableHla.alleleGroup() == alleleGroup
        val hlaProteinMatch = actionableHla.hlaProtein() == hlaProtein

        return geneMatch && alleleGroupMatch && hlaProteinMatch
    }
}