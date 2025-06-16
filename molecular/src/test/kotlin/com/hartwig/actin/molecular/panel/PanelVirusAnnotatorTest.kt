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
    fun `Should annotate sequenced virus correctly`() {
        val annotatedPanel = annotator.annotate(setOf(SequencedVirus("HPV", true)))
        assertThat(annotatedPanel).isEqualTo(
            listOf(
                Virus(
                    name = "Human papillomavirus",
                    type = VirusType.HUMAN_PAPILLOMA_VIRUS,
                    isReliable = true,
                    integrations = 1,
                    isReportable = true,
                    event = "HPV positive",
                    driverLikelihood = DriverLikelihood.HIGH,
                    evidence = TestClinicalEvidenceFactory.createEmpty()
                )
            )
        )
    }
}