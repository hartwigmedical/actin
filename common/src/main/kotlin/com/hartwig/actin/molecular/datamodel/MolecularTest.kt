package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import java.time.LocalDate

const val ARCHER_FP_LUNG_TARGET = "Archer FP Lung Target"
const val AVL_PANEL = "AvL Panel"
const val FREE_TEXT_PANEL = "Freetext"

interface MolecularTest {
    val type: ExperimentType
    val date: LocalDate?
    val drivers: Drivers
    val characteristics: MolecularCharacteristics
    val evidenceSource: String

    fun testsGene(gene: String): Boolean
}

private const val NONE = "none"

data class IHCMolecularTest(
    val test: PriorMolecularTest
) : MolecularTest {
    override val type = ExperimentType.IHC
    override val date = test.measureDate
    override val drivers = Drivers()
    override val characteristics = MolecularCharacteristics()
    override val evidenceSource = NONE

    override fun testsGene(gene: String) = test.measure == gene
}

data class OtherPriorMolecularTest(
    val test: PriorMolecularTest
) : MolecularTest {
    override val type = ExperimentType.OTHER
    override val date = test.measureDate
    override val drivers = Drivers()
    override val characteristics = MolecularCharacteristics()
    override val evidenceSource = NONE

    override fun testsGene(gene: String) = test.measure == gene
}
