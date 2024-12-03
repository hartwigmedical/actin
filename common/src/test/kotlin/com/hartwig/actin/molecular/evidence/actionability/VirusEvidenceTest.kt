package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory.minimalVirus
import com.hartwig.actin.datamodel.molecular.orange.driver.VirusType
import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class VirusEvidenceTest {

    @Test
    fun `Should determine evidence for HPV`() {
        val hpv = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.HPV_POSITIVE)
        val virusEvidence = VirusEvidence.create(evidences = listOf(hpv), trials = emptyList())

        val virusMatch = minimalVirus().copy(type = VirusType.HUMAN_PAPILLOMA_VIRUS, isReportable = true)
        val matches = virusEvidence.findMatches(virusMatch)
        assertThat(matches.evidenceMatches.size).isEqualTo(1)
        assertThat(matches.evidenceMatches).contains(hpv)

        val noInterpretation = minimalVirus().copy(type = VirusType.OTHER, isReportable = true)
        assertThat(virusEvidence.findMatches(noInterpretation).evidenceMatches).isEmpty()

        val otherInterpretation = minimalVirus().copy(type = VirusType.MERKEL_CELL_VIRUS, isReportable = true)
        assertThat(virusEvidence.findMatches(otherInterpretation).evidenceMatches).isEmpty()

        val notReported = minimalVirus().copy(type = VirusType.HUMAN_PAPILLOMA_VIRUS, isReportable = false)
        assertThat(virusEvidence.findMatches(notReported).evidenceMatches).isEmpty()
    }

    @Test
    fun `Should determine evidence for EBV`() {
        val ebv = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.EBV_POSITIVE)
        val virusEvidence = VirusEvidence.create(evidences = listOf(ebv), trials = emptyList())

        val virusMatch = minimalVirus().copy(type = VirusType.EPSTEIN_BARR_VIRUS, isReportable = true)
        val matches = virusEvidence.findMatches(virusMatch)
        assertThat(matches.evidenceMatches.size).isEqualTo(1)
        assertThat(matches.evidenceMatches).contains(ebv)

        val noInterpretation = minimalVirus().copy(type = VirusType.OTHER, isReportable = true)
        assertThat(virusEvidence.findMatches(noInterpretation).evidenceMatches).isEmpty()

        val otherInterpretation = minimalVirus().copy(type = VirusType.MERKEL_CELL_VIRUS, isReportable = true)
        assertThat(virusEvidence.findMatches(otherInterpretation).evidenceMatches).isEmpty()

        val notReported = minimalVirus().copy(type = VirusType.EPSTEIN_BARR_VIRUS, isReportable = false)
        assertThat(virusEvidence.findMatches(notReported).evidenceMatches).isEmpty()
    }
}