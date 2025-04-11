package com.hartwig.actin.datamodel.molecular

import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import java.time.LocalDate

data class PanelRecord(
    val geneCoverage: Map<String, List<MolecularTestTarget>>,
    override val experimentType: ExperimentType,
    override val testTypeDisplay: String? = null,
    override val date: LocalDate? = null,
    override val drivers: Drivers,
    override val characteristics: MolecularCharacteristics,
    override val evidenceSource: String,
    override val hasSufficientPurity: Boolean,
    override val hasSufficientQuality: Boolean
) : MolecularTest {

    override fun testsGene(gene: String, molecularTestTargets: List<MolecularTestTarget>): Boolean {
        return geneCoverage[gene]?.containsAll(molecularTestTargets) ?: false
    }
}