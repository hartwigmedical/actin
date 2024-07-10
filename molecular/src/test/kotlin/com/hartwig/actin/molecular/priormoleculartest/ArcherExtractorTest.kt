package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.panel.PanelVariantExtraction
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherFusionExtraction
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanelExtraction
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherSkippedExonsExtraction
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class ArcherExtractorTest {

    private val interpreter = ArcherExtractor()

    @Test
    fun `Should parse archer variants from prior molecular tests`() {
        val result = interpreter.extract(listOf(archerPriorMolecularVariantRecord(GENE, HGVS_CODING)))
        assertThat(result).containsExactly(ArcherPanelExtraction(variants = listOf(PanelVariantExtraction(GENE, HGVS_CODING))))
    }

    @Test
    fun `Should parse archer fusions from prior molecular tests`() {
        val result = interpreter.extract(listOf(archerPriorMolecularFusionRecord(GENE)))
        assertThat(result).containsExactly(ArcherPanelExtraction(fusions = listOf(ArcherFusionExtraction(GENE))))
    }

    @Test
    fun `Should parse archer exon skips from prior molecular tests`() {
        val result =
            interpreter.extract(
                listOf(
                    archerExonSkippingRecord(GENE, "1-2"),
                    archerExonSkippingRecord(GENE, "3")
                )
            )
        assertThat(result).containsExactly(
            ArcherPanelExtraction(
                skippedExons = listOf(
                    ArcherSkippedExonsExtraction(GENE, 1, 2),
                    ArcherSkippedExonsExtraction(GENE, 3, 3)
                )
            )
        )
    }

    @Test
    fun `Should throw illegal argument exception when unknown result`() {
        assertThatThrownBy {
            interpreter.extract(
                listOf(
                    PriorMolecularTest(
                        test = "Archer FP Lung Target",
                        item = GENE,
                        measure = "Unknown",
                        impliesPotentialIndeterminateStatus = false
                    )
                )
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
    }
}