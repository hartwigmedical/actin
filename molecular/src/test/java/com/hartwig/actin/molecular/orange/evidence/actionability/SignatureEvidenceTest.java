package com.hartwig.actin.molecular.orange.evidence.actionability;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.ImmutableActionableEvents;
import com.hartwig.serve.datamodel.characteristic.ActionableCharacteristic;
import com.hartwig.serve.datamodel.characteristic.TumorCharacteristicType;

import org.junit.Test;

public class SignatureEvidenceTest {

    @Test
    public void canDetermineEvidenceForMicrosatelliteInstability() {
        ActionableCharacteristic characteristic1 =
                TestServeActionabilityFactory.characteristicBuilder().type(TumorCharacteristicType.MICROSATELLITE_UNSTABLE).build();
        ActionableCharacteristic characteristic2 =
                TestServeActionabilityFactory.characteristicBuilder().type(TumorCharacteristicType.MICROSATELLITE_STABLE).build();
        ActionableEvents actionable = ImmutableActionableEvents.builder().addCharacteristics(characteristic1, characteristic2).build();

        SignatureEvidence signatureEvidence = SignatureEvidence.create(actionable);

        List<ActionableEvent> matches = signatureEvidence.findMicrosatelliteMatches(true);
        assertEquals(1, matches.size());
        assertTrue(matches.contains(characteristic1));

        assertTrue(signatureEvidence.findMicrosatelliteMatches(false).isEmpty());
    }

    @Test
    public void canDetermineEvidenceForHomologousRepairDeficiency() {
        ActionableCharacteristic characteristic1 = TestServeActionabilityFactory.characteristicBuilder()
                .type(TumorCharacteristicType.HOMOLOGOUS_RECOMBINATION_DEFICIENT)
                .build();
        ActionableEvents actionable = ImmutableActionableEvents.builder().addCharacteristics(characteristic1).build();

        SignatureEvidence signatureEvidence = SignatureEvidence.create(actionable);

        List<ActionableEvent> matches = signatureEvidence.findHomologousRepairMatches(true);
        assertEquals(1, matches.size());
        assertTrue(matches.contains(characteristic1));

        assertTrue(signatureEvidence.findHomologousRepairMatches(false).isEmpty());
    }

    @Test
    public void canDetermineEvidenceForHighTumorMutationalBurden() {
        ActionableCharacteristic characteristic1 = TestServeActionabilityFactory.characteristicBuilder()
                .type(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN)
                .build();
        ActionableCharacteristic characteristic2 = TestServeActionabilityFactory.characteristicBuilder()
                .type(TumorCharacteristicType.LOW_TUMOR_MUTATIONAL_BURDEN)
                .build();
        ActionableEvents actionable = ImmutableActionableEvents.builder().addCharacteristics(characteristic1, characteristic2).build();

        SignatureEvidence signatureEvidence = SignatureEvidence.create(actionable);

        List<ActionableEvent> matches = signatureEvidence.findTumorBurdenMatches(true);
        assertEquals(1, matches.size());
        assertTrue(matches.contains(characteristic1));

        assertTrue(signatureEvidence.findTumorBurdenMatches(false).isEmpty());
    }

    @Test
    public void canDetermineEvidenceForHighTumorMutationalLoad() {
        ActionableCharacteristic characteristic1 = TestServeActionabilityFactory.characteristicBuilder()
                .type(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_LOAD)
                .build();
        ActionableCharacteristic characteristic2 = TestServeActionabilityFactory.characteristicBuilder()
                .type(TumorCharacteristicType.LOW_TUMOR_MUTATIONAL_LOAD)
                .build();
        ActionableEvents actionable = ImmutableActionableEvents.builder().addCharacteristics(characteristic1, characteristic2).build();

        SignatureEvidence signatureEvidence = SignatureEvidence.create(actionable);

        List<ActionableEvent> matches = signatureEvidence.findTumorLoadMatches(true);
        assertEquals(1, matches.size());
        assertTrue(matches.contains(characteristic1));

        assertTrue(signatureEvidence.findTumorLoadMatches(false).isEmpty());
    }
}