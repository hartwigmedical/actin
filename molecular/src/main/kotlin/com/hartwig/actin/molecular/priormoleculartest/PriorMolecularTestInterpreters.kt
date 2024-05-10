package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.MolecularExtractor
import com.hartwig.actin.molecular.MolecularInterpreter
import com.hartwig.actin.molecular.datamodel.ARCHER_FP_LUNG_TARGET
import com.hartwig.actin.molecular.datamodel.AVL_PANEL
import com.hartwig.actin.molecular.datamodel.FREE_TEXT_PANEL
import com.hartwig.actin.molecular.datamodel.IHCMolecularTest
import com.hartwig.actin.molecular.datamodel.MolecularTest
import com.hartwig.actin.molecular.datamodel.OtherPriorMolecularTest
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanel
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanel
import com.hartwig.actin.molecular.evidence.EvidenceDatabase

private fun <T : MolecularTest> identityAnnotator() = object : MolecularAnnotator<T> {
    override fun annotate(input: T): T {
        return input
    }
}

private fun otherExtractor() = object : MolecularExtractor<PriorMolecularTest, MolecularTest> {
    override fun interpret(input: List<PriorMolecularTest>): List<MolecularTest> {
        return input.map { OtherPriorMolecularTest(it) }
    }
}

private fun ihcExtractor() = object : MolecularExtractor<PriorMolecularTest, IHCMolecularTest> {
    override fun interpret(input: List<PriorMolecularTest>): List<IHCMolecularTest> {
        return input.map { IHCMolecularTest(it) }
    }
}

private fun isArcher(): (PriorMolecularTest) -> Boolean = { it.test == ARCHER_FP_LUNG_TARGET }

private class ArcherInterpreter(evidenceDatabase: EvidenceDatabase) :
    MolecularInterpreter<PriorMolecularTest, ArcherPanel>(ArcherExtractor(), ArcherAnnotator(evidenceDatabase), isArcher())

private fun isGeneric(): (PriorMolecularTest) -> Boolean =
    { it.test == AVL_PANEL || it.test == FREE_TEXT_PANEL }

private class GenericPanelInterpreter :
    MolecularInterpreter<PriorMolecularTest, GenericPanel>(GenericPanelExtractor(), identityAnnotator(), isGeneric())

private fun isIHC(): (PriorMolecularTest) -> Boolean {
    return { it.test == "IHC" || it.item == "PD-L1" }
}

private class IHCInterpreter : MolecularInterpreter<PriorMolecularTest, IHCMolecularTest>(ihcExtractor(), identityAnnotator(), isIHC())

class PriorMolecularTestInterpreters(private val pipelines: Set<MolecularInterpreter<PriorMolecularTest, out MolecularTest>>) {

    fun process(clinicalTests: List<PriorMolecularTest>): List<MolecularTest> {
        val otherInterpreter = MolecularInterpreter(
            otherExtractor(),
            identityAnnotator()
        ) { test -> pipelines.none { it.inputPredicate.invoke(test) } }
        return (pipelines + otherInterpreter).flatMap { it.run(clinicalTests) }
    }

    companion object {
        fun create(evidenceDatabase: EvidenceDatabase) = PriorMolecularTestInterpreters(
            setOf(
                ArcherInterpreter(evidenceDatabase),
                GenericPanelInterpreter(),
                IHCInterpreter()
            )
        )
    }
}