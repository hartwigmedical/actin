package com.hartwig.actin.molecular.evidence.matching

import com.hartwig.actin.datamodel.molecular.immunology.HlaAllele
import com.hartwig.serve.datamodel.molecular.immuno.ActionableHLA

object ImmunologyMatching {

    fun isMatch(actionableHla: ActionableHLA, hlaAllele: HlaAllele): Boolean {
        val geneMatch = actionableHla.gene() == hlaAllele.gene
        val alleleGroupMatch = actionableHla.alleleGroup() == hlaAllele.alleleGroup
        val hlaProteinMatch = actionableHla.hlaProtein() == hlaAllele.hlaProtein

        return geneMatch && alleleGroupMatch && hlaProteinMatch
    }
}