package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.TestMolecularFactory.minimalVirus
import com.hartwig.actin.molecular.evidence.TestServeActionabilityFactory
import com.hartwig.actin.molecular.datamodel.orange.driver.VirusType
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.ImmutableActionableEvents
import com.hartwig.serve.datamodel.characteristic.ActionableCharacteristic
import com.hartwig.serve.datamodel.characteristic.TumorCharacteristicType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class VirusEvidenceTest {

    @Test
    fun `Should determine evidence for HPV`() {
        val hpv: ActionableCharacteristic =
            TestServeActionabilityFactory.characteristicBuilder().type(TumorCharacteristicType.HPV_POSITIVE).build()
        val actionable: ActionableEvents = ImmutableActionableEvents.builder().addCharacteristics(hpv).build()
        val virusEvidence: VirusEvidence = VirusEvidence.create(actionable)

        val virusMatch = minimalVirus().copy(type = VirusType.HUMAN_PAPILLOMA_VIRUS, isReportable = true)
        val matches = virusEvidence.findMatches(virusMatch)
        assertThat(matches.size).isEqualTo(1)
        assertThat(matches).contains(hpv)

        val noInterpretation = minimalVirus().copy(type = VirusType.OTHER, isReportable = true)
        assertThat(virusEvidence.findMatches(noInterpretation)).isEmpty()

        val otherInterpretation = minimalVirus().copy(type = VirusType.MERKEL_CELL_VIRUS, isReportable = true)
        assertThat(virusEvidence.findMatches(otherInterpretation)).isEmpty()

        val notReported = minimalVirus().copy(type = VirusType.HUMAN_PAPILLOMA_VIRUS, isReportable = false)
        assertThat(virusEvidence.findMatches(notReported)).isEmpty()
    }

    @Test
    fun `Should determine evidence for EBV`() {
        val ebv: ActionableCharacteristic =
            TestServeActionabilityFactory.characteristicBuilder().type(TumorCharacteristicType.EBV_POSITIVE).build()
        val actionable: ActionableEvents = ImmutableActionableEvents.builder().addCharacteristics(ebv).build()
        val virusEvidence: VirusEvidence = VirusEvidence.create(actionable)

        val virusMatch = minimalVirus().copy(type = VirusType.EPSTEIN_BARR_VIRUS, isReportable = true)
        val matches = virusEvidence.findMatches(virusMatch)
        assertThat(matches.size).isEqualTo(1)
        assertThat(matches).contains(ebv)

        val noInterpretation = minimalVirus().copy(type = VirusType.OTHER, isReportable = true)
        assertThat(virusEvidence.findMatches(noInterpretation)).isEmpty()

        val otherInterpretation = minimalVirus().copy(type = VirusType.MERKEL_CELL_VIRUS, isReportable = true)
        assertThat(virusEvidence.findMatches(otherInterpretation)).isEmpty()

        val notReported = minimalVirus().copy(type = VirusType.EPSTEIN_BARR_VIRUS, isReportable = false)
        assertThat(virusEvidence.findMatches(notReported)).isEmpty()
    }
}