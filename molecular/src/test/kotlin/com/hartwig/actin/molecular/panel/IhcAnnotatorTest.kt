package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.RefGenomeVersion
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.panel.PanelTargetSpecification
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val TESTED_GENE = "gene"

private val TEST_DATE = LocalDate.of(2023, 1, 1)

class IhcAnnotatorTest {

    private val ihcAnnotator = IhcAnnotator()

    private val baseMolecularTest = MolecularTest(
        date = TEST_DATE,
        sampleId = null,
        reportHash = null,
        experimentType = ExperimentType.IHC,
        testTypeDisplay = ExperimentType.IHC.display(),
        targetSpecification = PanelTargetSpecification(emptyMap()),
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
    fun `Should annotate IHC extraction with fusion tested genes`() {
        val ihcExtraction =
            IhcExtraction(date = TEST_DATE, fusionTestedGenes = setOf(TESTED_GENE), mutationAndDeletionTestedGenes = emptySet())
        val result = ihcAnnotator.annotate(ihcExtraction)

        assertThat(result).isEqualTo(
            baseMolecularTest.copy(
                targetSpecification = PanelTargetSpecification(
                    mapOf(
                        TESTED_GENE to listOf(
                            MolecularTestTarget.FUSION
                        )
                    )
                ),
            )
        )
    }

    @Test
    fun `Should annotate IHC extraction with mutation and deletion tested genes`() {
        val ihcExtraction =
            IhcExtraction(date = TEST_DATE, fusionTestedGenes = emptySet(), mutationAndDeletionTestedGenes = setOf(TESTED_GENE))
        val result = ihcAnnotator.annotate(ihcExtraction)

        assertThat(result).isEqualTo(
            baseMolecularTest.copy(
                targetSpecification = PanelTargetSpecification(
                    mapOf(
                        TESTED_GENE to listOf(
                            MolecularTestTarget.MUTATION,
                            MolecularTestTarget.DELETION
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should annotate IHC extraction with fusion, mutation and deletion tested genes if tested for both`() {
        val ihcExtraction =
            IhcExtraction(date = TEST_DATE, fusionTestedGenes = setOf(TESTED_GENE), mutationAndDeletionTestedGenes = setOf(TESTED_GENE))
        val result = ihcAnnotator.annotate(ihcExtraction)

        assertThat(result).isEqualTo(
            baseMolecularTest.copy(
                targetSpecification = PanelTargetSpecification(
                    mapOf(
                        TESTED_GENE to listOf(
                            MolecularTestTarget.FUSION,
                            MolecularTestTarget.MUTATION,
                            MolecularTestTarget.DELETION
                        )
                    )
                )
            )
        )
    }
}