package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.clinical.datamodel.PriorSequencingTest
import com.hartwig.actin.molecular.MolecularExtractor
import com.hartwig.actin.molecular.datamodel.panel.*

class PriorSequencingExtractor : MolecularExtractor<PriorSequencingTest, PanelExtraction> {
    override fun extract(input: List<PriorSequencingTest>): List<PanelExtraction> {
        return input.map { PanelExtractionAdapter(it) }
    }
}

data class PanelExtractionAdapter(val priorSequencingTest: PriorSequencingTest) : PanelExtraction {
    override val panelType = priorSequencingTest.test
    override val tumorMutationalBurden = priorSequencingTest.tumorMutationalBurden
    override val isMicrosatelliteUnstable = priorSequencingTest.isMicrosatelliteUnstable
    override val variants = priorSequencingTest.variants.map {
        PanelVariantExtraction(
            it.gene,
            it.hgvsCodingImpact ?: it.hgvsProteinImpact ?: throw IllegalStateException()
        )
    }
    override val fusions = priorSequencingTest.fusions.map {
        PanelFusionExtraction(it.geneUp, it.geneDown)
    }
    override val amplifications = priorSequencingTest.amplifications.map {
        PanelAmplificationExtraction(it.gene)
    }
    override val date = priorSequencingTest.date
    override val extractionClass: String = PanelExtractionAdapter::class.java.simpleName

    override fun testedGenes() = priorSequencingTest.testedGenes ?: emptySet()

    override fun events() = emptySet<PanelEvent>()
}