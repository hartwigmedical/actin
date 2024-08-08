package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.clinical.datamodel.PriorIHCTest
import com.hartwig.actin.molecular.datamodel.AVL_PANEL
import com.hartwig.actin.molecular.datamodel.FREE_TEXT_PANEL
import com.hartwig.actin.molecular.datamodel.panel.PanelFusionExtraction
import com.hartwig.actin.molecular.datamodel.panel.PanelVariantExtraction
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanelExtraction
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class GenericPanelExtractorTest {

    private val extractor = GenericPanelExtractor()

    @Test
    fun `Should distinguish generic panel types`() {
        val genericPanelTests = listOf(
            avlPanelPriorMolecularNoMutationsFoundRecord(),
            freetextPriorMolecularFusionRecord()
        )
        val molecularTests = extractor.extract(genericPanelTests)
        assertThat(molecularTests).hasSize(2)
    }

    @Test
    fun `Should construct AvL panel from prior molecular`() {
        val priorMolecularTests = listOf(
            avlPanelPriorMolecularNoMutationsFoundRecord(),
            avlPanelPriorMolecularVariantRecord()
        )
        val molecularTests = extractor.extract(priorMolecularTests)

        val expected = GenericPanelExtraction(
            panelType = AVL_PANEL,
            variants = listOf(PanelVariantExtraction(GENE, HGVS_CODING))
        )
        assertThat(molecularTests).containsExactly(expected)
    }

    @Test
    fun `Should construct Freetext panel from prior molecular`() {
        val priorMolecularTests = listOf(freetextPriorMolecularFusionRecord())
        val molecularTests = extractor.extract(priorMolecularTests)

        val expected = GenericPanelExtraction(
            panelType = FREE_TEXT_PANEL,
            variants = emptyList(),
            fusions = listOf(PanelFusionExtraction(GENE_UP, GENE_DOWN))
        )
        assertThat(molecularTests).containsExactly(expected)
    }

    @Test
    fun `Should extract protein and coding variants`() {
        val priorMolecularTests = listOf(
            freetextPriorMolecularVariantRecord(GENE, HGVS_CODING),
            freetextPriorMolecularVariantRecord(GENE, HGVS_PROTEIN)
        )
        val molecularTests = extractor.extract(priorMolecularTests)

        val expected = GenericPanelExtraction(
            panelType = FREE_TEXT_PANEL,
            variants = listOf(
                PanelVariantExtraction(GENE, HGVS_CODING),
                PanelVariantExtraction(GENE, HGVS_PROTEIN)
            )
        )
        assertThat(molecularTests).containsExactly(expected)
    }

    @Test
    fun `Should filter unknown record`() {
        val record = PriorIHCTest(
            test = "Freetext",
            item = "KRAS A1Z",
            measure = null,
            impliesPotentialIndeterminateStatus = false
        )
        val priorMolecularTests = listOf(record)
        assertThat(extractor.extract(priorMolecularTests)).containsOnly(GenericPanelExtraction(panelType = FREE_TEXT_PANEL))
    }

    @Test
    fun `Should extract negative results for genes and include them in tested genes`() {
        val priorMolecularTests = listOf(
            freetextPriorMolecularNegativeGeneRecord(GENE)
        )
        val molecularTests = extractor.extract(priorMolecularTests)

        val expected = GenericPanelExtraction(
            panelType = FREE_TEXT_PANEL,
            genesWithNegativeResults = setOf(GENE)
        )
        assertThat(molecularTests).containsExactly(expected)
        assertThat(expected.testedGenes()).contains(GENE)
    }
}