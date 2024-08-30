package com.hartwig.actin.datamodel.molecular

import java.time.LocalDate

data class PanelRecord(
    val testedGenes: Set<String>,
    override val experimentType: ExperimentType,
    override val testTypeDisplay: String? = null,
    override val date: LocalDate? = null,
    override val drivers: Drivers,
    override val characteristics: MolecularCharacteristics = MolecularCharacteristics(),
    override val evidenceSource: String
) : MolecularTest {

    override fun testsGene(gene: String): Boolean {
        return testedGenes.contains(gene)
    }
}