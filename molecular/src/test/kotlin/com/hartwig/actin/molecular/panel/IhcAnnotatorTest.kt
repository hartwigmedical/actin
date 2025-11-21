package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencedFusion
import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.RefGenomeVersion
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.panel.PanelTargetSpecification
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private val TEST_DATE = LocalDate.of(2023, 1, 1)
private const val POSITIVE_FUSION_GENE = "ALK"
private const val NEGATIVE_FUSION_GENE = "ROS1"
private const val MUTATION_OR_DELETED_TESTED_GENE = "MTAP"

class IhcAnnotatorTest {

    private val fusion = mockk<Fusion> {
        every { geneStart } returns POSITIVE_FUSION_GENE
        every { geneEnd } returns ""
    }

    private val panelFusionAnnotator = mockk<PanelFusionAnnotator> {
        every { annotate(setOf(SequencedFusion(POSITIVE_FUSION_GENE)), emptySet()) } returns listOf(fusion)
        every { annotate(emptySet(), emptySet()) } returns emptyList()
    }

    private val ihcAnnotator = IhcAnnotator(panelFusionAnnotator)

    private val baseMolecularTest = MolecularTest(
        date = TEST_DATE,
        sampleId = null,
        reportHash = null,
        experimentType = ExperimentType.IHC,
        testTypeDisplay = ExperimentType.IHC.display(),
        targetSpecification = null,
        refGenomeVersion = RefGenomeVersion.V37,
        containsTumorCells = true,
        hasSufficientPurity = true,
        hasSufficientQuality = true,
        isContaminated = false,
        drivers = TestMolecularFactory.createMinimalTestDrivers(),
        characteristics = TestMolecularFactory.createMinimalTestCharacteristics(),
        immunology = null,
        pharmaco = emptySet(),
        evidenceSource = ActionabilityConstants.EVIDENCE_SOURCE.display(),
        externalTrialSource = ActionabilityConstants.EXTERNAL_TRIAL_SOURCE.display()
    )

    @Test
    fun `Should annotate IHC extraction with positive fusion genes`() {
        val ihcExtraction = IhcExtraction(
            TEST_DATE,
            fusionPositiveGenes = setOf(POSITIVE_FUSION_GENE),
            fusionNegativeGenes = emptySet(),
            mutationAndDeletionTestedGenes = emptySet()
        )

        val result = ihcAnnotator.annotate(ihcExtraction)

        assertThat(result).isEqualTo(
            baseMolecularTest.copy(
                targetSpecification = PanelTargetSpecification(
                    mapOf(POSITIVE_FUSION_GENE to listOf(MolecularTestTarget.FUSION))
                ),
                drivers = TestMolecularFactory.createMinimalTestDrivers().copy(fusions = listOf(fusion))
            )
        )
    }

    @Test
    fun `Should annotate IHC extraction with negative fusion genes`() {
        val ihcExtraction = IhcExtraction(
            date = TEST_DATE,
            fusionPositiveGenes = emptySet(),
            fusionNegativeGenes = setOf(NEGATIVE_FUSION_GENE),
            mutationAndDeletionTestedGenes = emptySet()
        )

        val result = ihcAnnotator.annotate(ihcExtraction)

        assertThat(result).isEqualTo(
            baseMolecularTest.copy(
                targetSpecification = PanelTargetSpecification(
                    mapOf(NEGATIVE_FUSION_GENE to listOf(MolecularTestTarget.FUSION))
                )
            )
        )
    }

    @Test
    fun `Should annotate IHC extraction with mutation and deletion tested genes`() {
        val ihcExtraction = IhcExtraction(
            date = TEST_DATE,
            fusionPositiveGenes = emptySet(),
            fusionNegativeGenes = emptySet(),
            mutationAndDeletionTestedGenes = setOf(MUTATION_OR_DELETED_TESTED_GENE)
        )

        val result = ihcAnnotator.annotate(ihcExtraction)

        assertThat(result).isEqualTo(
            baseMolecularTest.copy(
                targetSpecification = PanelTargetSpecification(
                    mapOf(
                        MUTATION_OR_DELETED_TESTED_GENE to listOf(MolecularTestTarget.MUTATION, MolecularTestTarget.DELETION)
                    )
                )
            )
        )
    }
}