package com.hartwig.actin.datamodel.molecular.panel

import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import java.time.LocalDate
import java.util.function.Predicate

data class PanelTargetSpecification(
    private val geneTargetMap: Map<String, List<MolecularTestTarget>>,
    val versionDate: LocalDate? = null,
    val testDateIsBeforeOldestTestVersion: Boolean = false
) {
    
    fun testsGene(gene: String, molecularTestTargets: Predicate<List<MolecularTestTarget>>) =
        geneTargetMap[gene]?.let { molecularTestTargets.test(it) } ?: false
}
