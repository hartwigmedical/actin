package com.hartwig.actin.molecular.datamodel.panel

import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.MolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.MolecularTest
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanelExtraction
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanelExtraction
import java.time.LocalDate

data class PanelRecord(
    val archerPanelExtraction: ArcherPanelExtraction? = null,
    val genericPanelExtraction: GenericPanelExtraction? = null,
    override val type: ExperimentType,
    override val date: LocalDate? = null,
    override val drivers: PanelDrivers,
    override val characteristics: MolecularCharacteristics = MolecularCharacteristics(),
    override val evidenceSource: String,
) : MolecularTest<PanelDrivers> {

    fun testedGenes() = archerPanelExtraction?.testedGenes() ?: genericPanelExtraction?.testedGenes() ?: emptySet()

    override fun isGeneTested(gene: String): Boolean {
        return testedGenes().contains(gene)
    }

    fun events(): Set<PanelEvent> {
        return archerPanelExtraction?.events()?.toSet() ?: genericPanelExtraction?.events()?.toSet() ?: emptySet()
    }

}