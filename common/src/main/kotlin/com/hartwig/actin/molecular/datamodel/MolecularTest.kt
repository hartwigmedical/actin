package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import java.time.LocalDate

const val ARCHER_FP_LUNG_TARGET = "Archer FP Lung Target"
const val AVL_PANEL = "AvL Panel"
const val FREE_TEXT_PANEL = "Freetext"

interface MolecularTest {
    val experimentType: ExperimentType
    val testTypeDisplay: String?
    val date: LocalDate?
    val drivers: Drivers
    val characteristics: MolecularCharacteristics
    val evidenceSource: String

    fun testsGene(gene: String): Boolean
}

const val NO_EVIDENCE_SOURCE = "none"

data class IHCMolecularTest(
    val test: PriorMolecularTest
) : MolecularTest {
    override val experimentType = ExperimentType.IHC
    override val testTypeDisplay = null
    override val date = test.measureDate
    override val drivers = Drivers()
    override val characteristics = MolecularCharacteristics()
    override val evidenceSource = NO_EVIDENCE_SOURCE

    override fun testsGene(gene: String) = test.measure == gene
}

data class OtherPriorMolecularTest(
    val test: PriorMolecularTest
) : MolecularTest {
    override val experimentType = ExperimentType.OTHER
    override val testTypeDisplay = null
    override val date = test.measureDate
    override val drivers = Drivers()
    override val characteristics = MolecularCharacteristics()
    override val evidenceSource = NO_EVIDENCE_SOURCE

    override fun testsGene(gene: String) = test.measure == gene
}
