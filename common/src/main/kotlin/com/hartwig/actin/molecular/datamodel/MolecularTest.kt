package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import java.time.LocalDate

const val ARCHER_FP_LUNG_TARGET = "Archer FP Lung Target"
const val AVL_PANEL = "AvL Panel"
const val FREE_TEXT_PANEL = "Freetext"

interface MolecularTest {
    val type: ExperimentType
    val date: LocalDate?
}

data class IHCMolecularTest(
    val test: PriorMolecularTest
) : MolecularTest {
    override val type = ExperimentType.IHC
    override val date = test.measureDate
}

data class OtherPriorMolecularTest(
    val test: PriorMolecularTest
) : MolecularTest {
    override val type = ExperimentType.OTHER
    override val date = test.measureDate
}