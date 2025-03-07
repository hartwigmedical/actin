package com.hartwig.actin.datamodel.molecular

import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import java.time.LocalDate

data class PanelRecord(
    val testedGenes: Set<String>,
    override val experimentType: ExperimentType,
    override val testTypeDisplay: String? = null,
    override val date: LocalDate? = null,
    override val drivers: Drivers,
    override val characteristics: MolecularCharacteristics = MolecularCharacteristics(),
    override val evidenceSource: String,
    override val hasSufficientPurity: Boolean,
    override val hasSufficientQuality: Boolean
) : MolecularTest {

    override fun testsGene(gene: String): Boolean {
        return testedGenes.contains(gene)
    }
}