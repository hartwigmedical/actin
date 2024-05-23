package com.hartwig.actin.molecular.datamodel.panel

import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.MolecularTest
import com.hartwig.actin.molecular.datamodel.characteristics.MolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanelExtraction
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanelExtraction
import java.time.LocalDate

data class PanelRecord(
    val testedGenes: Set<String>,
    val archerPanelExtraction: ArcherPanelExtraction? = null,
    val genericPanelExtraction: GenericPanelExtraction? = null,
    override val type: ExperimentType,
    override val date: LocalDate? = null,
    override val drivers: PanelDrivers,
    override val characteristics: MolecularCharacteristics = MolecularCharacteristics(),
    override val evidenceSource: String,
    ) : MolecularTest<PanelDrivers> {

    override fun isGeneTested(gene: String): Boolean {
        return testedGenes.contains(gene)
    }

    fun events(): Set<PanelEvent> {
        return drivers.variants + drivers.fusions
    }
}