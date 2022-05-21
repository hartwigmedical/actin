package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.molecular.orange.datamodel.protect.EvidenceType;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectEvidence;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectTestFactory;

import org.junit.Test;

public class EvidenceEventExtractionTest {

    @Test
    public void canExtractEventFromEvidence() {
        ProtectEvidence evidence1 = ProtectTestFactory.builder()
                .gene("gene")
                .event("event")
                .addSources(ProtectTestFactory.sourceBuilder().type(EvidenceType.ANY_MUTATION).build())
                .build();
        assertEquals("gene event", EvidenceEventExtraction.extract(evidence1));

        ProtectEvidence evidence2 = ProtectTestFactory.builder()
                .event("event")
                .addSources(ProtectTestFactory.sourceBuilder().type(EvidenceType.ANY_MUTATION).build())
                .build();
        assertEquals("event", EvidenceEventExtraction.extract(evidence2));

        ProtectEvidence evidence3 = ProtectTestFactory.builder()
                .gene("gene")
                .event("event")
                .addSources(ProtectTestFactory.sourceBuilder().type(EvidenceType.PROMISCUOUS_FUSION).build())
                .build();
        assertEquals("event", EvidenceEventExtraction.extract(evidence3));
    }

}