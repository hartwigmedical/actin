package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import java.time.LocalDate

const val ARCHER_FP_LUNG_TARGET = "Archer FP Lung Target"
const val AVL_PANEL = "AvL Panel"
const val FREE_TEXT_PANEL = "Freetext"

interface MolecularTest<D : Drivers<out Variant, out Fusion>> {
    val type: ExperimentType
    val date: LocalDate?
    val drivers: D
    val characteristics: MolecularCharacteristics
    val evidenceSource: String

    fun testsGene(gene: String): Boolean
}

private const val NONE = "none"

data class IHCMolecularTest(
    val test: PriorMolecularTest
) : MolecularTest<UnknownDrivers> {
    override val type = ExperimentType.IHC
    override val date = test.measureDate
    override val drivers = UnknownDrivers()
    override val evidenceSource = NONE
    override val characteristics = MolecularCharacteristics()


    override fun testsGene(gene: String) = test.measure == gene
}

data class OtherPriorMolecularTest(
    val test: PriorMolecularTest
) : MolecularTest<UnknownDrivers> {
    override val type = ExperimentType.OTHER
    override val date = test.measureDate
    override val drivers = UnknownDrivers()
    override val evidenceSource = NONE
    override val characteristics = MolecularCharacteristics()

    override fun testsGene(gene: String) = test.measure == gene
}

class UnknownDrivers : Drivers<Variant, Fusion> {
    override val variants = emptySet<Variant>()
    override val fusions = emptySet<Fusion>()
}