package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.clinical.datamodel.PriorIHCTest
import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.MolecularExtractor
import com.hartwig.actin.molecular.MolecularInterpreter
import com.hartwig.actin.molecular.datamodel.*
import com.hartwig.actin.molecular.datamodel.panel.PanelExtraction
import com.hartwig.actin.molecular.datamodel.panel.PanelRecord
import com.hartwig.actin.molecular.driverlikelihood.GeneDriverLikelihoodModel
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.paver.Paver
import com.hartwig.actin.tools.pave.PaveLite
import com.hartwig.actin.tools.variant.VariantAnnotator
import com.hartwig.hmftools.common.fusion.KnownFusionCache

private fun <T : MolecularTest> identityAnnotator() = object : MolecularAnnotator<T, T> {
    override fun annotate(input: T): T {
        return input
    }
}

private fun otherExtractor() = object : MolecularExtractor<PriorIHCTest, OtherPriorMolecularTest> {
    override fun extract(input: List<PriorIHCTest>): List<OtherPriorMolecularTest> {
        return input.map { OtherPriorMolecularTest(it) }
    }
}


private fun isArcher(): (PriorIHCTest) -> Boolean = { it.test == ARCHER_FP_LUNG_TARGET }

private class ArcherInterpreter(
    evidenceDatabase: EvidenceDatabase,
    geneDriverLikelihoodModel: GeneDriverLikelihoodModel,
    variantAnnotator: VariantAnnotator,
    paver: Paver,
    paveLite: PaveLite,
    knownFusionCache: KnownFusionCache
) :
    MolecularInterpreter<PriorIHCTest, PanelExtraction, PanelRecord>(
        ArcherExtractor(),
        PanelAnnotator(evidenceDatabase, geneDriverLikelihoodModel, variantAnnotator, paver, paveLite, knownFusionCache),
        isArcher()
    )

private fun isGeneric(): (PriorIHCTest) -> Boolean =
    { it.test == AVL_PANEL || it.test == FREE_TEXT_PANEL || it.test.startsWith("NGS") }

private fun isMcgi(): (PriorIHCTest) -> Boolean =
    { it.test.lowercase().contains("cdx") }

private class GenericPanelInterpreter(
    evidenceDatabase: EvidenceDatabase,
    geneDriverLikelihoodModel: GeneDriverLikelihoodModel,
    variantAnnotator: VariantAnnotator,
    paver: Paver,
    paveLite: PaveLite,
    knownFusionCache: KnownFusionCache
) :
    MolecularInterpreter<PriorIHCTest, PanelExtraction, PanelRecord>(
        GenericPanelExtractor(),
        PanelAnnotator(evidenceDatabase, geneDriverLikelihoodModel, variantAnnotator, paver, paveLite, knownFusionCache),
        isGeneric()
    )

private class McgiPanelInterpreter(
    evidenceDatabase: EvidenceDatabase,
    geneDriverLikelihoodModel: GeneDriverLikelihoodModel,
    variantAnnotator: VariantAnnotator,
    paver: Paver,
    paveLite: PaveLite,
    knownFusionCache: KnownFusionCache
) :
    MolecularInterpreter<PriorIHCTest, PanelExtraction, PanelRecord>(
        McgiExtractor(),
        PanelAnnotator(evidenceDatabase, geneDriverLikelihoodModel, variantAnnotator, paver, paveLite, knownFusionCache),
        isMcgi()
    )

class PriorMolecularTestInterpreters(private val pipelines: Set<MolecularInterpreter<PriorIHCTest, out Any, out MolecularTest>>) {

    fun process(clinicalTests: List<PriorIHCTest>): List<MolecularTest> {
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
            paveLite: PaveLite,
            knownFusionCache: KnownFusionCache
        ) =
            PriorMolecularTestInterpreters(
                setOf(
                    ArcherInterpreter(evidenceDatabase, geneDriverLikelihoodModel, variantAnnotator, paver, paveLite, knownFusionCache),
                    GenericPanelInterpreter(
                        evidenceDatabase,
                        geneDriverLikelihoodModel,
                        variantAnnotator,
                        paver,
                        paveLite,
                        knownFusionCache
                    ),
                    McgiPanelInterpreter(evidenceDatabase, geneDriverLikelihoodModel, variantAnnotator, paver, paveLite, knownFusionCache)
                )
            )
    }
}