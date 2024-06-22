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
import com.hartwig.actin.molecular.datamodel.panel.PanelRecord
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanelExtraction
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanelExtraction
import com.hartwig.actin.molecular.driverlikelihood.GeneDriverLikelihoodModel
import com.hartwig.actin.molecular.evidence.EvidenceDatabase

private fun <T : MolecularTest> identityAnnotator() = object : MolecularAnnotator<T, T> {
    override fun annotate(input: T): T {
        return input
    }
}

private fun otherExtractor() = object : MolecularExtractor<PriorMolecularTest, OtherPriorMolecularTest> {
    override fun extract(input: List<PriorMolecularTest>): List<OtherPriorMolecularTest> {
        return input.map { OtherPriorMolecularTest(it) }
    }
}

private fun ihcExtractor() = object : MolecularExtractor<PriorMolecularTest, IHCMolecularTest> {
    override fun extract(input: List<PriorMolecularTest>): List<IHCMolecularTest> {
        return input.map { IHCMolecularTest(it) }
    }
}

private fun isArcher(): (PriorMolecularTest) -> Boolean = { it.test == ARCHER_FP_LUNG_TARGET }

private class ArcherInterpreter(evidenceDatabase: EvidenceDatabase, geneDriverLikelihoodModel: GeneDriverLikelihoodModel) :
    MolecularInterpreter<PriorMolecularTest, ArcherPanelExtraction, PanelRecord>(
        ArcherExtractor(),
        ArcherAnnotator(evidenceDatabase, geneDriverLikelihoodModel),
        isArcher()
    )

private fun isGeneric(): (PriorMolecularTest) -> Boolean =
    { it.test == AVL_PANEL || it.test == FREE_TEXT_PANEL }

private class GenericPanelInterpreter :
    MolecularInterpreter<PriorMolecularTest, GenericPanelExtraction, PanelRecord>(
        GenericPanelExtractor(),
        GenericPanelAnnotator(),
        isGeneric()
    )

private fun isIHC(): (PriorMolecularTest) -> Boolean {
    return { it.test == "IHC" || it.item == "PD-L1" }
}

private class IHCInterpreter :
    MolecularInterpreter<PriorMolecularTest, IHCMolecularTest, IHCMolecularTest>(ihcExtractor(), identityAnnotator(), isIHC())

class PriorMolecularTestInterpreters(private val pipelines: Set<MolecularInterpreter<PriorMolecularTest, out Any, out MolecularTest>>) {

    fun process(clinicalTests: List<PriorMolecularTest>): List<MolecularTest> {
        val otherInterpreter = MolecularInterpreter(otherExtractor(), identityAnnotator()) { test ->
            pipelines.none { it.inputPredicate.invoke(test) }
        }
        return (pipelines + otherInterpreter).flatMap { it.run(clinicalTests) }
    }

    companion object {
        fun create(evidenceDatabase: EvidenceDatabase, geneDriverLikelihoodModel: GeneDriverLikelihoodModel) =
            PriorMolecularTestInterpreters(
                setOf(
                    ArcherInterpreter(evidenceDatabase, geneDriverLikelihoodModel),
                    GenericPanelInterpreter(),
                    IHCInterpreter()
                )
            )
    }
}