package com.hartwig.actin.molecular.panel

import com.hartwig.actin.configuration.MolecularConfiguration
import com.hartwig.actin.datamodel.clinical.SequencedVirus
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.Virus
import com.hartwig.actin.datamodel.molecular.driver.VirusType
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

val VIRUS = VirusType.HPV
val LOW_RISK_SEQUENCED_VIRUS = SequencedVirus(VIRUS, isLowRisk = true)
val EXPECTED_LOW_RISK_ANNOTATED_VIRUS = Virus(
    name = "Human papillomavirus",
    type = VIRUS,
    isReliable = true,
    integrations = null,
    isReportable = true,
    event = "HPV positive",
    driverLikelihood = DriverLikelihood.LOW,
    evidence = TestClinicalEvidenceFactory.createEmpty()
)

class PanelVirusAnnotatorTest {

    private val configuration = MolecularConfiguration()
    private val annotator = PanelVirusAnnotator(configuration)

    @Test
    fun `Should annotate low risk virus as expected`() {
        val annotatedPanel = annotator.annotate(setOf(LOW_RISK_SEQUENCED_VIRUS))
        assertThat(annotatedPanel).isEqualTo(listOf(EXPECTED_LOW_RISK_ANNOTATED_VIRUS))
    }

    @Test
    fun `Should annotate high risk virus with high driver likelihood`() {
        val annotatedPanel = annotator.annotate(setOf(LOW_RISK_SEQUENCED_VIRUS.copy(isLowRisk = false)))
        assertThat(annotatedPanel).isEqualTo(listOf(EXPECTED_LOW_RISK_ANNOTATED_VIRUS.copy(driverLikelihood = DriverLikelihood.HIGH)))
    }

    @Test
    fun `Should annotate low risk virus with high driver likelihood if event pathogenicity is confirmed`() {
        val annotator = PanelVirusAnnotator(MolecularConfiguration(eventPathogenicityIsConfirmed = true))
        val annotatedPanel = annotator.annotate(setOf(LOW_RISK_SEQUENCED_VIRUS))
        assertThat(annotatedPanel).isEqualTo(listOf(EXPECTED_LOW_RISK_ANNOTATED_VIRUS.copy(driverLikelihood = DriverLikelihood.HIGH)))
    }
}