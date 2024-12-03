package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SignatureEvidenceTest {

    @Test
    fun `Should determine evidence for microsatellite instability`() {
        val characteristic1: EfficacyEvidence =
            TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.MICROSATELLITE_UNSTABLE)
        val characteristic2: EfficacyEvidence =
            TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.MICROSATELLITE_STABLE)
        val actionable = ActionableEvents(listOf(characteristic1, characteristic2), emptyList())
        val signatureEvidence: SignatureEvidence = SignatureEvidence.create(actionable)

        val matches = signatureEvidence.findMicrosatelliteMatches(true)
        assertThat(matches.evidences.size).isEqualTo(1)
        assertThat(matches.evidences).contains(characteristic1)
        assertThat(signatureEvidence.findMicrosatelliteMatches(false).evidences).isEmpty()
    }

    @Test
    fun `Should determine evidence for homologous repair deficiency`() {
        val characteristic1: EfficacyEvidence =
            TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.HOMOLOGOUS_RECOMBINATION_DEFICIENT)
        val actionable = ActionableEvents(listOf(characteristic1), emptyList())
        val signatureEvidence: SignatureEvidence = SignatureEvidence.create(actionable)

        val matches = signatureEvidence.findHomologousRepairMatches(true)
        assertThat(matches.evidences.size).isEqualTo(1)
        assertThat(matches.evidences).contains(characteristic1)
        assertThat(signatureEvidence.findHomologousRepairMatches(false).evidences).isEmpty()
    }

    @Test
    fun `Should determine evidence for high tumor mutational burden`() {
        val characteristic1: EfficacyEvidence =
            TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN)
        val characteristic2: EfficacyEvidence =
            TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.LOW_TUMOR_MUTATIONAL_BURDEN)
        val actionable = ActionableEvents(listOf(characteristic1, characteristic2), emptyList())
        val signatureEvidence: SignatureEvidence = SignatureEvidence.create(actionable)

        val matches = signatureEvidence.findTumorBurdenMatches(true)
        assertThat(matches.evidences.size).isEqualTo(1)
        assertThat(matches.evidences).contains(characteristic1)
        assertThat(signatureEvidence.findTumorBurdenMatches(false).evidences).isEmpty()
    }

    @Test
    fun `Should determine evidence for high mutational load`() {
        val characteristic1: EfficacyEvidence =
            TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_LOAD)
        val characteristic2: EfficacyEvidence =
            TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.LOW_TUMOR_MUTATIONAL_LOAD)
        val actionable = ActionableEvents(listOf(characteristic1, characteristic2), emptyList())
        val signatureEvidence: SignatureEvidence = SignatureEvidence.create(actionable)

        val matches = signatureEvidence.findTumorLoadMatches(true)
        assertThat(matches.evidences.size).isEqualTo(1)
        assertThat(matches.evidences).contains(characteristic1)
        assertThat(signatureEvidence.findTumorLoadMatches(false).evidences).isEmpty()
    }
}