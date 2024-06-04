package com.hartwig.actin.molecular.datamodel.panel

import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.MolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.MolecularTest
import java.time.LocalDate

data class PanelRecord(
    val testedGenes: Set<String>,
    val panelEvents: Set<PanelEvent>,
    override val type: ExperimentType,
    override val date: LocalDate? = null,
    override val drivers: PanelDrivers,
    override val characteristics: MolecularCharacteristics = MolecularCharacteristics(),
    override val evidenceSource: String,
) : MolecularTest<PanelDrivers> {

    fun testedGenes() = testedGenes

    override fun testsGene(gene: String): Boolean {
        return testedGenes().contains(gene)
    }

    fun events(): Set<PanelEvent> {
        return panelEvents
    }
}