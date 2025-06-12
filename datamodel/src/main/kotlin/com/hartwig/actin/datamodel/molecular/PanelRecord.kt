package com.hartwig.actin.datamodel.molecular

import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import java.time.LocalDate
import java.util.function.Predicate

data class PanelRecord(
    val specification: PanelSpecification,
    override val experimentType: ExperimentType,
    override val testTypeDisplay: String? = null,
    override val date: LocalDate? = null,
    override val drivers: Drivers,
    override val characteristics: MolecularCharacteristics,
    override val evidenceSource: String,
    override val hasSufficientPurity: Boolean,
    override val hasSufficientQuality: Boolean,
    val reportHash: String? = null
) : MolecularTest {

    override fun testsGene(gene: String, molecularTestTargets: Predicate<List<MolecularTestTarget>>) =
        specification.testsGene(gene, molecularTestTargets)
}