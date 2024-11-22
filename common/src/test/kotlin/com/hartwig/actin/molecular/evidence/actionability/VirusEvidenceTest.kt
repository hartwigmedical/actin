package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory.minimalVirus
import com.hartwig.actin.datamodel.molecular.orange.driver.VirusType
import com.hartwig.actin.molecular.evidence.TestServeActionabilityFactory
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class VirusEvidenceTest {

    @Test
    fun `Should determine evidence for HPV`() {
        val hpv: EfficacyEvidence =
            TestServeActionabilityFactory.createEfficacyEvidenceWithCharacteristic(TumorCharacteristicType.HPV_POSITIVE)
        val actionable = ActionableEvents(listOf(hpv), emptyList())
        val virusEvidence: VirusEvidence = VirusEvidence.create(actionable)

        val virusMatch = minimalVirus().copy(type = VirusType.HUMAN_PAPILLOMA_VIRUS, isReportable = true)
        val matches = virusEvidence.findMatches(virusMatch)
        assertThat(matches.evidences.size).isEqualTo(1)
        assertThat(matches.evidences).contains(hpv)

        val noInterpretation = minimalVirus().copy(type = VirusType.OTHER, isReportable = true)
        assertThat(virusEvidence.findMatches(noInterpretation).evidences).isEmpty()

        val otherInterpretation = minimalVirus().copy(type = VirusType.MERKEL_CELL_VIRUS, isReportable = true)
        assertThat(virusEvidence.findMatches(otherInterpretation).evidences).isEmpty()

        val notReported = minimalVirus().copy(type = VirusType.HUMAN_PAPILLOMA_VIRUS, isReportable = false)
        assertThat(virusEvidence.findMatches(notReported).evidences).isEmpty()
    }

    @Test
    fun `Should determine evidence for EBV`() {
        val ebv: EfficacyEvidence =
            TestServeActionabilityFactory.createEfficacyEvidenceWithCharacteristic(TumorCharacteristicType.EBV_POSITIVE)
        val actionable = ActionableEvents(listOf(ebv), emptyList())
        val virusEvidence: VirusEvidence = VirusEvidence.create(actionable)

        val virusMatch = minimalVirus().copy(type = VirusType.EPSTEIN_BARR_VIRUS, isReportable = true)
        val matches = virusEvidence.findMatches(virusMatch)
        assertThat(matches.evidences.size).isEqualTo(1)
        assertThat(matches.evidences).contains(ebv)

        val noInterpretation = minimalVirus().copy(type = VirusType.OTHER, isReportable = true)
        assertThat(virusEvidence.findMatches(noInterpretation).evidences).isEmpty()

        val otherInterpretation = minimalVirus().copy(type = VirusType.MERKEL_CELL_VIRUS, isReportable = true)
        assertThat(virusEvidence.findMatches(otherInterpretation).evidences).isEmpty()

        val notReported = minimalVirus().copy(type = VirusType.EPSTEIN_BARR_VIRUS, isReportable = false)
        assertThat(virusEvidence.findMatches(notReported).evidences).isEmpty()
    }
}