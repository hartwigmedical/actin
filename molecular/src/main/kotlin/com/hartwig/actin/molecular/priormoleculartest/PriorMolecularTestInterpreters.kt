package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.clinical.datamodel.PriorSequencingTest
import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.MolecularExtractor
import com.hartwig.actin.molecular.MolecularInterpreter
import com.hartwig.actin.molecular.datamodel.ARCHER_FP_LUNG_TARGET
import com.hartwig.actin.molecular.datamodel.AVL_PANEL
import com.hartwig.actin.molecular.datamodel.FREE_TEXT_PANEL
import com.hartwig.actin.molecular.datamodel.MolecularTest
import com.hartwig.actin.molecular.datamodel.OtherPriorMolecularTest
import com.hartwig.actin.molecular.datamodel.panel.PanelExtraction
import com.hartwig.actin.molecular.datamodel.panel.PanelRecord
import com.hartwig.actin.molecular.driverlikelihood.GeneDriverLikelihoodModel
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.paver.Paver
import com.hartwig.actin.tools.pave.PaveLite
import com.hartwig.actin.tools.variant.VariantAnnotator

private fun <T : MolecularTest> identityAnnotator() = object : MolecularAnnotator<T, T> {
    override fun annotate(input: T): T {
        return input
    }
}

private fun otherExtractor() = object : MolecularExtractor<PriorSequencingTest, OtherPriorMolecularTest> {
    override fun extract(input: List<PriorSequencingTest>): List<OtherPriorMolecularTest> {
        return input.map { OtherPriorMolecularTest(it) }
    }
}

private fun isArcher(): (PriorSequencingTest) -> Boolean = { it.test == ARCHER_FP_LUNG_TARGET }

private class ArcherInterpreter(
    evidenceDatabase: EvidenceDatabase,
    geneDriverLikelihoodModel: GeneDriverLikelihoodModel,
    variantAnnotator: VariantAnnotator,
    paver: Paver,
    paveLite: PaveLite
) :
    MolecularInterpreter<PriorSequencingTest, PanelExtraction, PanelRecord>(
        ArcherExtractor(),
        PanelAnnotator(evidenceDatabase, geneDriverLikelihoodModel, variantAnnotator, paver, paveLite),
        isArcher()
    )

private fun isGeneric(): (PriorSequencingTest) -> Boolean =
    { it.test == AVL_PANEL || it.test == FREE_TEXT_PANEL }

private fun isMcgi(): (PriorSequencingTest) -> Boolean =
    { it.test.lowercase().contains("cdx") }

private class GenericPanelInterpreter(
    evidenceDatabase: EvidenceDatabase,
    geneDriverLikelihoodModel: GeneDriverLikelihoodModel,
    variantAnnotator: VariantAnnotator,
    paver: Paver,
    paveLite: PaveLite
) :
    MolecularInterpreter<PriorSequencingTest, PanelExtraction, PanelRecord>(
        GenericPanelExtractor(),
        PanelAnnotator(evidenceDatabase, geneDriverLikelihoodModel, variantAnnotator, paver, paveLite),
        isGeneric()
    )

private class McgiPanelInterpreter(
    evidenceDatabase: EvidenceDatabase,
    geneDriverLikelihoodModel: GeneDriverLikelihoodModel,
    variantAnnotator: VariantAnnotator,
    paver: Paver,
    paveLite: PaveLite
) :
    MolecularInterpreter<PriorSequencingTest, PanelExtraction, PanelRecord>(
        McgiExtractor(),
        PanelAnnotator(evidenceDatabase, geneDriverLikelihoodModel, variantAnnotator, paver, paveLite),
        isMcgi()
    )

class PriorMolecularTestInterpreters(private val pipelines: Set<MolecularInterpreter<PriorSequencingTest, out Any, out MolecularTest>>) {

    fun process(clinicalTests: List<PriorSequencingTest>): List<MolecularTest> {
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
            paver: Paver,
            paveLite: PaveLite
        ) =
            PriorMolecularTestInterpreters(
                setOf(
                    ArcherInterpreter(evidenceDatabase, geneDriverLikelihoodModel, variantAnnotator, paver, paveLite),
                    GenericPanelInterpreter(evidenceDatabase, geneDriverLikelihoodModel, variantAnnotator, paver, paveLite),
                    McgiPanelInterpreter(evidenceDatabase, geneDriverLikelihoodModel, variantAnnotator, paver, paveLite)
                )
            )
    }
}