package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericFusion
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanel
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanelType
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericVariant
import org.assertj.core.api.Assertions
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
        assertThat(molecularTests.filter { it.type == ExperimentType.GENERIC_PANEL }).hasSize(2)

    }

    @Test
    fun `Should construct AvL panel from prior molecular`() {
        val priorMolecularTests = listOf(
            avlPanelPriorMolecularNoMutationsFoundRecord(),
            avlPanelPriorMolecularVariantRecord()
        )
        val molecularTests = extractor.extract(priorMolecularTests)

        val expected = GenericPanel(GenericPanelType.AVL, variants = listOf(GenericVariant(GENE, HGVS_CODING)))
        assertThat(molecularTests).containsExactly(expected)
    }

    @Test
    fun `Should construct Freetext panel from prior molecular`() {
        val priorMolecularTests = listOf(freetextPriorMolecularFusionRecord())
        val molecularTests = extractor.extract(priorMolecularTests)

        val expected = GenericPanel(
            GenericPanelType.FREE_TEXT,
            variants = emptyList(),
            fusions = listOf(GenericFusion(GENE_UP, GENE_DOWN))
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

        val expected = GenericPanel(
            GenericPanelType.FREE_TEXT,
            variants = listOf(
                GenericVariant(GENE, HGVS_CODING),
                GenericVariant(GENE, HGVS_PROTEIN)
            )
        )
        assertThat(molecularTests).containsExactly(expected)
    }

    @Test
    fun `Should throw exception on unextractable freetext record`() {
        val record = PriorMolecularTest(
            test = "Freetext",
            item = "KRAS A1Z",
            measure = null,
            impliesPotentialIndeterminateStatus = false
        )
        val priorMolecularTests = listOf(record)
        Assertions.assertThatThrownBy {
            extractor.extract(priorMolecularTests)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }
}