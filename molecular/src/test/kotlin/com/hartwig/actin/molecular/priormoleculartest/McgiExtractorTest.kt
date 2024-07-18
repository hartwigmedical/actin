package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.panel.PanelAmplificationExtraction
import com.hartwig.actin.molecular.datamodel.panel.PanelVariantExtraction
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


private val TEST_DATE = LocalDate.of(2024, 7, 17)

private const val CHROMOSOME = "chr1"

private const val PANEL_TYPE = "test"

class McgiExtractorTest {

    private val extractor = McgiExtractor()

    @Test
    fun `Should return empty extraction when no tests`() {
        val result = extractor.extract(emptyList())
        assertThat(result).isEmpty()
    }

    @Test
    fun `Should extract variants from prior molecular tests`() {
        val result = extractor.extract(listOf(priorMolecularTest(HGVS_PROTEIN, "variant")))
        assertThat(result.first().variants).containsExactly(PanelVariantExtraction(GENE, HGVS_PROTEIN))
    }

    @Test
    fun `Should extract amplifications from prior molecular tests`() {
        val result = extractor.extract(listOf(priorMolecularTest(CHROMOSOME, "amplification")))
        assertThat(result.first().amplifications).containsExactly(PanelAmplificationExtraction(GENE, CHROMOSOME))
    }

    @Test
    fun `Should extract tmb from prior molecular tests`() {
        val result = extractor.extract(listOf(priorMolecularTest("1.0", "tmb")))
        assertThat(result.first().tumorMutationalBurden).isEqualTo(1.0)
    }

    @Test
    fun `Should extract msi from prior molecular tests`() {
        val result = extractor.extract(listOf(priorMolecularTest("true", "msi")))
        assertThat(result.first().isMicrosatelliteUnstable).isTrue()
    }

    @Test
    fun `Should group multiple tests together by date and test type`() {
        val protein2 = "protein 2"
        val chromosome2 = "chr2"
        val protein3 = "protein 3"
        val chromosome3 = "chr3"
        val panelType2 = "test 2"
        val testDate2 = TEST_DATE.plusDays(1)
        val result = extractor.extract(
            listOf(
                priorMolecularTest(HGVS_PROTEIN, "variant"),
                priorMolecularTest(CHROMOSOME, "amplification"),
                priorMolecularTest(protein2, "variant", date = testDate2),
                priorMolecularTest(chromosome2, "amplification", date = testDate2),
                priorMolecularTest(protein3, "variant", test = panelType2),
                priorMolecularTest(chromosome3, "amplification", test = panelType2)
            )
        )
        assertThat(result.size).isEqualTo(3)
        assertThat(result[0].panelType).isEqualTo(PANEL_TYPE)
        assertThat(result[0].date).isEqualTo(TEST_DATE)
        assertThat(result[0].variants).containsExactly(PanelVariantExtraction(GENE, HGVS_PROTEIN))
        assertThat(result[0].amplifications).containsExactly(PanelAmplificationExtraction(GENE, CHROMOSOME))
        assertThat(result[1].panelType).isEqualTo(PANEL_TYPE)
        assertThat(result[1].date).isEqualTo(testDate2)
        assertThat(result[1].variants).containsExactly(PanelVariantExtraction(GENE, protein2))
        assertThat(result[1].amplifications).containsExactly(PanelAmplificationExtraction(GENE, chromosome2))
        assertThat(result[2].panelType).isEqualTo(panelType2)
        assertThat(result[2].date).isEqualTo(TEST_DATE)
        assertThat(result[2].variants).containsExactly(PanelVariantExtraction(GENE, protein3))
        assertThat(result[2].amplifications).containsExactly(PanelAmplificationExtraction(GENE, chromosome3))
    }

    private fun priorMolecularTest(measure: String, type: String, test: String = PANEL_TYPE, date: LocalDate = TEST_DATE) =
        PriorMolecularTest(
            test = test,
            item = GENE,
            measure = measure,
            measureDate = date,
            scoreText = type,
            impliesPotentialIndeterminateStatus = false
        )

}