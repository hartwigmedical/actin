package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SignatureEvidenceTest {

    @Test
    fun `Should determine evidence for microsatellite instability`() {
        val characteristic1 = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.MICROSATELLITE_UNSTABLE)
        val characteristic2 = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.MICROSATELLITE_STABLE)
        val signatureEvidence = SignatureEvidence.create(evidences = listOf(characteristic1, characteristic2), trials = emptyList())

        val matches = signatureEvidence.findMicrosatelliteMatches(true)
        assertThat(matches.evidenceMatches.size).isEqualTo(1)
        assertThat(matches.evidenceMatches).contains(characteristic1)
        assertThat(signatureEvidence.findMicrosatelliteMatches(false).evidenceMatches).isEmpty()
    }

    @Test
    fun `Should determine evidence for homologous repair deficiency`() {
        val characteristic1 =
            TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.HOMOLOGOUS_RECOMBINATION_DEFICIENT)
        val signatureEvidence = SignatureEvidence.create(evidences = listOf(characteristic1), trials = emptyList())

        val matches = signatureEvidence.findHomologousRepairMatches(true)
        assertThat(matches.evidenceMatches.size).isEqualTo(1)
        assertThat(matches.evidenceMatches).contains(characteristic1)
        assertThat(signatureEvidence.findHomologousRepairMatches(false).evidenceMatches).isEmpty()
    }

    @Test
    fun `Should determine evidence for high tumor mutational burden`() {
        val characteristic1 = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN)
        val characteristic2 = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.LOW_TUMOR_MUTATIONAL_BURDEN)
        val signatureEvidence = SignatureEvidence.create(evidences = listOf(characteristic1, characteristic2), trials = emptyList())

        val matches = signatureEvidence.findTumorBurdenMatches(true)
        assertThat(matches.evidenceMatches.size).isEqualTo(1)
        assertThat(matches.evidenceMatches).contains(characteristic1)
        assertThat(signatureEvidence.findTumorBurdenMatches(false).evidenceMatches).isEmpty()
    }

    @Test
    fun `Should determine evidence for high mutational load`() {
        val characteristic1 = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_LOAD)
        val characteristic2 = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.LOW_TUMOR_MUTATIONAL_LOAD)
        val signatureEvidence = SignatureEvidence.create(evidences = listOf(characteristic1, characteristic2), trials = emptyList())

        val matches = signatureEvidence.findTumorLoadMatches(true)
        assertThat(matches.evidenceMatches.size).isEqualTo(1)
        assertThat(matches.evidenceMatches).contains(characteristic1)
        assertThat(signatureEvidence.findTumorLoadMatches(false).evidenceMatches).isEmpty()
    }
}