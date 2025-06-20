package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencedVirus
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.Virus
import com.hartwig.actin.datamodel.molecular.driver.VirusType
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PanelVirusAnnotatorTest {

    private val annotator = PanelVirusAnnotator()

    @Test
    fun `Should annotate HPV low risk sequenced virus correctly`() {
        val annotatedPanel = annotator.annotate(setOf(SequencedVirus(VirusType.HPV, isLowRisk = true)))
        assertThat(annotatedPanel).isEqualTo(
            listOf(
                Virus(
                    name = "Human papillomavirus",
                    type = VirusType.HPV,
                    isReliable = true,
                    integrations = null,
                    isReportable = true,
                    event = "HPV positive",
                    driverLikelihood = DriverLikelihood.LOW,
                    evidence = TestClinicalEvidenceFactory.createEmpty()
                )
            )
        )
    }

    @Test
    fun `Should annotate other sequenced virus correctly`() {
        val annotatedPanel = annotator.annotate(setOf(SequencedVirus(VirusType.EBV, isLowRisk = false)))
        assertThat(annotatedPanel).isEqualTo(
            listOf(
                Virus(
                    name = "Epstein-Barr virus",
                    type = VirusType.EBV,
                    isReliable = true,
                    integrations = null,
                    isReportable = true,
                    event = "EBV positive",
                    driverLikelihood = DriverLikelihood.HIGH,
                    evidence = TestClinicalEvidenceFactory.createEmpty()
                )
            )
        )
    }
}