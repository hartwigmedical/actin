package com.hartwig.actin.molecular.datamodel.panel

import com.hartwig.actin.molecular.datamodel.Drivers
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.MolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.MolecularTest
import java.time.LocalDate

data class PanelRecord(
    val panelExtraction: PanelExtraction,
    override val experimentType: ExperimentType,
    override val testTypeDisplay: String? = null,
    override val date: LocalDate? = null,
    override val drivers: Drivers,
    override val characteristics: MolecularCharacteristics = MolecularCharacteristics(),
    override val evidenceSource: String
) : MolecularTest {

    fun testedGenes() = panelExtraction.testedGenes()

    override fun testsGene(gene: String): Boolean {
        return testedGenes().contains(gene)
    }

    fun events(): Set<PanelEvent> {
        return panelExtraction.events()
    }
}