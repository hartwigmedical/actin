package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencedFusion
import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.PanelRecord
import com.hartwig.actin.datamodel.molecular.PanelSpecification
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val TEST_DATE = LocalDate.of(2023, 1, 1)
private const val POSITIVE_FUSION_GENE = "ALK"
private const val NEGATIVE_FUSION_GENE = "ROS1"

class IHCAnnotatorTest {

    private val fusion = mockk<Fusion> {
        every { geneStart } returns POSITIVE_FUSION_GENE
        every { geneEnd } returns ""
    }

    private val panelFusionAnnotator = mockk<PanelFusionAnnotator> {
        every { annotate(setOf(SequencedFusion(POSITIVE_FUSION_GENE)), emptySet()) } returns listOf(fusion)
        every { annotate(emptySet(), emptySet()) } returns emptyList()
    }

    private val ihcAnnotator = IHCAnnotator(panelFusionAnnotator)

    @Test
    fun `Should annotate IHC extraction with positive fusion genes`() {
        val ihcExtraction = IHCExtraction(
            TEST_DATE,
            fusionPositiveGenes = setOf(POSITIVE_FUSION_GENE),
            fusionNegativeGenes = emptySet()
        )

        val result = ihcAnnotator.annotate(ihcExtraction)

        assertThat(result).isEqualTo(
            PanelRecord(
                date = TEST_DATE,
                specification = PanelSpecification(mapOf("ALK" to listOf(MolecularTestTarget.FUSION))),
                experimentType = ExperimentType.IHC,
                testTypeDisplay = ExperimentType.IHC.display(),
                drivers = TestMolecularFactory.createMinimalTestDrivers().copy(fusions = listOf(fusion)),
                characteristics = TestMolecularFactory.createMinimalTestCharacteristics(),
                evidenceSource = ActionabilityConstants.EVIDENCE_SOURCE.display(),
                hasSufficientPurity = true,
                hasSufficientQuality = true
            )
        )
    }

    @Test
    fun `Should annotate IHC extraction with negative fusion genes`() {
        val ihcExtraction = IHCExtraction(
            date = TEST_DATE,
            fusionPositiveGenes = emptySet(),
            fusionNegativeGenes = setOf(NEGATIVE_FUSION_GENE)
        )

        val result = ihcAnnotator.annotate(ihcExtraction)

        assertThat(result).isEqualTo(
            PanelRecord(
                date = TEST_DATE,
                specification = PanelSpecification(mapOf(NEGATIVE_FUSION_GENE to listOf(MolecularTestTarget.FUSION))),
                experimentType = ExperimentType.IHC,
                testTypeDisplay = ExperimentType.IHC.display(),
                drivers = TestMolecularFactory.createMinimalTestDrivers(),
                characteristics = TestMolecularFactory.createMinimalTestCharacteristics(),
                evidenceSource = ActionabilityConstants.EVIDENCE_SOURCE.display(),
                hasSufficientPurity = true,
                hasSufficientQuality = true
            )
        )
    }
}