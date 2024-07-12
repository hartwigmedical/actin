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
import com.hartwig.actin.molecular.datamodel.panel.PanelExtraction
import com.hartwig.actin.molecular.datamodel.panel.PanelRecord
import com.hartwig.actin.molecular.driverlikelihood.GeneDriverLikelihoodModel
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.tools.pave.PaveLite
import com.hartwig.actin.tools.variant.VariantAnnotator

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

private class ArcherInterpreter(
    evidenceDatabase: EvidenceDatabase,
    geneDriverLikelihoodModel: GeneDriverLikelihoodModel,
    variantAnnotator: VariantAnnotator,
    paveLite: PaveLite
) :
    MolecularInterpreter<PriorMolecularTest, PanelExtraction, PanelRecord>(
        ArcherExtractor(),
        PanelAnnotator(evidenceDatabase, geneDriverLikelihoodModel, variantAnnotator, paveLite),
        isArcher()
    )

private fun isGeneric(): (PriorMolecularTest) -> Boolean =
    { it.test == AVL_PANEL || it.test == FREE_TEXT_PANEL }

private fun isMcgi(): (PriorMolecularTest) -> Boolean =
    { it.test.contains("CDx") }

private class GenericPanelInterpreter(
    evidenceDatabase: EvidenceDatabase,
    geneDriverLikelihoodModel: GeneDriverLikelihoodModel,
    variantAnnotator: VariantAnnotator,
    paveLite: PaveLite
) :
    MolecularInterpreter<PriorMolecularTest, PanelExtraction, PanelRecord>(
        GenericPanelExtractor(),
        PanelAnnotator(evidenceDatabase, geneDriverLikelihoodModel, variantAnnotator, paveLite),
        isGeneric()
    )

private class McgiPanelInterpreter(
    evidenceDatabase: EvidenceDatabase,
    geneDriverLikelihoodModel: GeneDriverLikelihoodModel,
    variantAnnotator: VariantAnnotator,
    paveLite: PaveLite
) :
    MolecularInterpreter<PriorMolecularTest, PanelExtraction, PanelRecord>(
        McgiExtractor(),
        PanelAnnotator(evidenceDatabase, geneDriverLikelihoodModel, variantAnnotator, paveLite),
        isMcgi()
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
        fun create(
            evidenceDatabase: EvidenceDatabase,
            geneDriverLikelihoodModel: GeneDriverLikelihoodModel,
            variantAnnotator: VariantAnnotator,
            paveLite: PaveLite
        ) =
            PriorMolecularTestInterpreters(
                setOf(
                    ArcherInterpreter(evidenceDatabase, geneDriverLikelihoodModel, variantAnnotator, paveLite),
                    GenericPanelInterpreter(evidenceDatabase, geneDriverLikelihoodModel, variantAnnotator, paveLite),
                    IHCInterpreter(),
                    McgiPanelInterpreter(evidenceDatabase, geneDriverLikelihoodModel, variantAnnotator, paveLite)
                )
            )
    }
}