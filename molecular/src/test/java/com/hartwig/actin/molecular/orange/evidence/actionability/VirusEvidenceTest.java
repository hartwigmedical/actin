package com.hartwig.actin.molecular.orange.evidence.actionability;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.hartwig.actin.molecular.orange.datamodel.virus.TestVirusInterpreterFactory;
import com.hartwig.hmftools.datamodel.virus.AnnotatedVirus;
import com.hartwig.hmftools.datamodel.virus.VirusInterpretation;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.ImmutableActionableEvents;
import com.hartwig.serve.datamodel.characteristic.ActionableCharacteristic;
import com.hartwig.serve.datamodel.characteristic.TumorCharacteristicType;

import org.junit.Test;

public class VirusEvidenceTest {

    @Test
    public void canDetermineEvidenceForHPV() {
        ActionableCharacteristic hpv =
                TestServeActionabilityFactory.characteristicBuilder().type(TumorCharacteristicType.HPV_POSITIVE).build();

        ActionableEvents actionable = ImmutableActionableEvents.builder().addCharacteristics(hpv).build();

        VirusEvidence virusEvidence = VirusEvidence.create(actionable);

        AnnotatedVirus virusMatch = TestVirusInterpreterFactory.builder().interpretation(VirusInterpretation.HPV).reported(true).build();

        List<ActionableEvent> matches = virusEvidence.findMatches(virusMatch);
        assertEquals(1, matches.size());
        assertTrue(matches.contains(hpv));

        AnnotatedVirus noInterpretation = TestVirusInterpreterFactory.builder().interpretation(null).reported(true).build();
        assertTrue(virusEvidence.findMatches(noInterpretation).isEmpty());

        AnnotatedVirus otherInterpretation =
                TestVirusInterpreterFactory.builder().interpretation(VirusInterpretation.MCV).reported(true).build();
        assertTrue(virusEvidence.findMatches(otherInterpretation).isEmpty());

        AnnotatedVirus notReported = TestVirusInterpreterFactory.builder().interpretation(VirusInterpretation.HPV).reported(false).build();
        assertTrue(virusEvidence.findMatches(notReported).isEmpty());
    }

    @Test
    public void canDetermineEvidenceForEBV() {
        ActionableCharacteristic ebv =
                TestServeActionabilityFactory.characteristicBuilder().type(TumorCharacteristicType.EBV_POSITIVE).build();

        ActionableEvents actionable = ImmutableActionableEvents.builder().addCharacteristics(ebv).build();

        VirusEvidence virusEvidence = VirusEvidence.create(actionable);

        AnnotatedVirus virusMatch = TestVirusInterpreterFactory.builder().interpretation(VirusInterpretation.EBV).reported(true).build();

        List<ActionableEvent> matches = virusEvidence.findMatches(virusMatch);
        assertEquals(1, matches.size());
        assertTrue(matches.contains(ebv));

        AnnotatedVirus noInterpretation = TestVirusInterpreterFactory.builder().interpretation(null).reported(true).build();
        assertTrue(virusEvidence.findMatches(noInterpretation).isEmpty());

        AnnotatedVirus otherInterpretation =
                TestVirusInterpreterFactory.builder().interpretation(VirusInterpretation.MCV).reported(true).build();
        assertTrue(virusEvidence.findMatches(otherInterpretation).isEmpty());

        AnnotatedVirus notReported = TestVirusInterpreterFactory.builder().interpretation(VirusInterpretation.EBV).reported(false).build();
        assertTrue(virusEvidence.findMatches(notReported).isEmpty());
    }
}