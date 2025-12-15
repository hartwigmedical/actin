package com.hartwig.actin.molecular.evidence.matching

import com.hartwig.actin.datamodel.molecular.driver.HlaAllele
import com.hartwig.serve.datamodel.molecular.immuno.ActionableHLA

object ImmunologyMatching {

    fun isMatch(actionableHla: ActionableHLA, hlaAllele: HlaAllele): Boolean {
        val regex = Regex(pattern = """^(?<gene>[A-Z]+)\*(?<alleleGroup>\d{2}):(?<hlaProtein>\d{2})$""")
        val match = regex.matchEntire(hlaAllele.name) ?: throw IllegalStateException("no valid hla")
        val gene = match.groups["gene"]!!.value
        val alleleGroup = match.groups["alleleGroup"]!!.value
        val hlaProtein = match.groups["hlaProtein"]!!.value

        val geneMatch = actionableHla.gene() == "HLA-$gene"
        val alleleGroupMatch = actionableHla.alleleGroup() == alleleGroup
        val hlaProteinMatch = actionableHla.hlaProtein() == hlaProtein

        return geneMatch && alleleGroupMatch && hlaProteinMatch
    }
}