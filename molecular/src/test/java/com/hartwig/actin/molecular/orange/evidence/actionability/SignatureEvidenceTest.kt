package com.hartwig.actin.molecular.orange.evidence.actionability

import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.ImmutableActionableEvents
import com.hartwig.serve.datamodel.characteristic.ActionableCharacteristic
import com.hartwig.serve.datamodel.characteristic.TumorCharacteristicType
import org.junit.Assert
import org.junit.Test

class SignatureEvidenceTest {
    @Test
    fun canDetermineEvidenceForMicrosatelliteInstability() {
        val characteristic1: ActionableCharacteristic = TestServeActionabilityFactory.characteristicBuilder().type(TumorCharacteristicType.MICROSATELLITE_UNSTABLE).build()
        val characteristic2: ActionableCharacteristic = TestServeActionabilityFactory.characteristicBuilder().type(TumorCharacteristicType.MICROSATELLITE_STABLE).build()
        val actionable: ActionableEvents = ImmutableActionableEvents.builder().addCharacteristics(characteristic1, characteristic2).build()
        val signatureEvidence: SignatureEvidence = SignatureEvidence.Companion.create(actionable)
        val matches = signatureEvidence.findMicrosatelliteMatches(true)
        Assert.assertEquals(1, matches.size.toLong())
        Assert.assertTrue(matches.contains(characteristic1))
        Assert.assertTrue(signatureEvidence.findMicrosatelliteMatches(false).isEmpty())
    }

    @Test
    fun canDetermineEvidenceForHomologousRepairDeficiency() {
        val characteristic1: ActionableCharacteristic = TestServeActionabilityFactory.characteristicBuilder()
            .type(TumorCharacteristicType.HOMOLOGOUS_RECOMBINATION_DEFICIENT)
            .build()
        val actionable: ActionableEvents = ImmutableActionableEvents.builder().addCharacteristics(characteristic1).build()
        val signatureEvidence: SignatureEvidence = SignatureEvidence.Companion.create(actionable)
        val matches = signatureEvidence.findHomologousRepairMatches(true)
        Assert.assertEquals(1, matches.size.toLong())
        Assert.assertTrue(matches.contains(characteristic1))
        Assert.assertTrue(signatureEvidence.findHomologousRepairMatches(false).isEmpty())
    }

    @Test
    fun canDetermineEvidenceForHighTumorMutationalBurden() {
        val characteristic1: ActionableCharacteristic = TestServeActionabilityFactory.characteristicBuilder()
            .type(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN)
            .build()
        val characteristic2: ActionableCharacteristic? = TestServeActionabilityFactory.characteristicBuilder()
            .type(TumorCharacteristicType.LOW_TUMOR_MUTATIONAL_BURDEN)
            .build()
        val actionable: ActionableEvents = ImmutableActionableEvents.builder().addCharacteristics(characteristic1, characteristic2).build()
        val signatureEvidence: SignatureEvidence = SignatureEvidence.Companion.create(actionable)
        val matches = signatureEvidence.findTumorBurdenMatches(true)
        Assert.assertEquals(1, matches.size.toLong())
        Assert.assertTrue(matches.contains(characteristic1))
        Assert.assertTrue(signatureEvidence.findTumorBurdenMatches(false).isEmpty())
    }

    @Test
    fun canDetermineEvidenceForHighTumorMutationalLoad() {
        val characteristic1: ActionableCharacteristic = TestServeActionabilityFactory.characteristicBuilder()
            .type(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_LOAD)
            .build()
        val characteristic2: ActionableCharacteristic? = TestServeActionabilityFactory.characteristicBuilder()
            .type(TumorCharacteristicType.LOW_TUMOR_MUTATIONAL_LOAD)
            .build()
        val actionable: ActionableEvents = ImmutableActionableEvents.builder().addCharacteristics(characteristic1, characteristic2).build()
        val signatureEvidence: SignatureEvidence = SignatureEvidence.Companion.create(actionable)
        val matches = signatureEvidence.findTumorLoadMatches(true)
        Assert.assertEquals(1, matches.size.toLong())
        Assert.assertTrue(matches.contains(characteristic1))
        Assert.assertTrue(signatureEvidence.findTumorLoadMatches(false).isEmpty())
    }
}