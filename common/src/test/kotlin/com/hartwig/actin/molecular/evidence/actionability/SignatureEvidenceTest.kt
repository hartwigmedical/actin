package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.evidence.TestServeActionabilityFactory
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.ImmutableActionableEvents
import com.hartwig.serve.datamodel.characteristic.ActionableCharacteristic
import com.hartwig.serve.datamodel.characteristic.TumorCharacteristicType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SignatureEvidenceTest {

    @Test
    fun `Should determine evidence for microsatellite instability`() {
        val characteristic1: ActionableCharacteristic =
            TestServeActionabilityFactory.characteristicBuilder().type(TumorCharacteristicType.MICROSATELLITE_UNSTABLE).build()
        val characteristic2: ActionableCharacteristic =
            TestServeActionabilityFactory.characteristicBuilder().type(TumorCharacteristicType.MICROSATELLITE_STABLE).build()
        val actionable: ActionableEvents = ImmutableActionableEvents.builder().addCharacteristics(characteristic1, characteristic2).build()
        val signatureEvidence: SignatureEvidence = SignatureEvidence.create(actionable)

        val matches = signatureEvidence.findMicrosatelliteMatches(true)
        assertThat(matches.size).isEqualTo(1)
        assertThat(matches).contains(characteristic1)
        assertThat(signatureEvidence.findMicrosatelliteMatches(false)).isEmpty()
    }

    @Test
    fun `Should determine evidence for homologous repair deficiency`() {
        val characteristic1: ActionableCharacteristic = TestServeActionabilityFactory.characteristicBuilder()
            .type(TumorCharacteristicType.HOMOLOGOUS_RECOMBINATION_DEFICIENT)
            .build()
        val actionable: ActionableEvents = ImmutableActionableEvents.builder().addCharacteristics(characteristic1).build()
        val signatureEvidence: SignatureEvidence = SignatureEvidence.create(actionable)

        val matches = signatureEvidence.findHomologousRepairMatches(true)
        assertThat(matches.size).isEqualTo(1)
        assertThat(matches).contains(characteristic1)
        assertThat(signatureEvidence.findHomologousRepairMatches(false)).isEmpty()
    }

    @Test
    fun `Should determine evidence for high tumor mutational burden`() {
        val characteristic1: ActionableCharacteristic = TestServeActionabilityFactory.characteristicBuilder()
            .type(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN)
            .build()
        val characteristic2: ActionableCharacteristic? = TestServeActionabilityFactory.characteristicBuilder()
            .type(TumorCharacteristicType.LOW_TUMOR_MUTATIONAL_BURDEN)
            .build()
        val actionable: ActionableEvents = ImmutableActionableEvents.builder().addCharacteristics(characteristic1, characteristic2).build()
        val signatureEvidence: SignatureEvidence = SignatureEvidence.create(actionable)

        val matches = signatureEvidence.findTumorBurdenMatches(true)
        assertThat(matches.size).isEqualTo(1)
        assertThat(matches).contains(characteristic1)
        assertThat(signatureEvidence.findTumorBurdenMatches(false)).isEmpty()
    }

    @Test
    fun `Should determine evidence for high mutational load`() {
        val characteristic1: ActionableCharacteristic = TestServeActionabilityFactory.characteristicBuilder()
            .type(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_LOAD)
            .build()
        val characteristic2: ActionableCharacteristic? = TestServeActionabilityFactory.characteristicBuilder()
            .type(TumorCharacteristicType.LOW_TUMOR_MUTATIONAL_LOAD)
            .build()
        val actionable: ActionableEvents = ImmutableActionableEvents.builder().addCharacteristics(characteristic1, characteristic2).build()
        val signatureEvidence: SignatureEvidence = SignatureEvidence.create(actionable)

        val matches = signatureEvidence.findTumorLoadMatches(true)
        assertThat(matches.size).isEqualTo(1)
        assertThat(matches).contains(characteristic1)
        assertThat(signatureEvidence.findTumorLoadMatches(false)).isEmpty()
    }
}