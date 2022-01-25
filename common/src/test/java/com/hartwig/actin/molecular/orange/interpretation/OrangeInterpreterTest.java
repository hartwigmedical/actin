package com.hartwig.actin.molecular.orange.interpretation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.orange.datamodel.EvidenceDirection;
import com.hartwig.actin.molecular.orange.datamodel.EvidenceLevel;
import com.hartwig.actin.molecular.orange.datamodel.ImmutableOrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.ImmutableTreatmentEvidence;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeDataFactory;
import com.hartwig.actin.molecular.orange.datamodel.TreatmentEvidence;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class OrangeInterpreterTest {

    @Test
    public void canInterpretMolecularRecord() {
        OrangeRecord record = recordWithEvidence(createTestEvidences());

        MolecularRecord interpretation = OrangeInterpreter.interpret(record);

        //        Set<String> eventsWithTrialEligibility = interpretation.eventsWithTrialEligibility();
        //        assertEquals(1, eventsWithTrialEligibility.size());
        //        assertTrue(eventsWithTrialEligibility.contains("Event 20"));
        //
        //        Set<String> iclusionApplicableEvent = interpretation.iclusionApplicableEvents();
        //        assertEquals(1, iclusionApplicableEvent.size());
        //        assertTrue(iclusionApplicableEvent.contains("Event 12"));
        //
        //        Set<String> ckbResponsiveEvents = interpretation.ckbApplicableResponsiveEvents();
        //        assertEquals(1, ckbResponsiveEvents.size());
        //        assertTrue(ckbResponsiveEvents.contains("Event 1"));
        //
        //        Set<String> ckbResistanceEvents = interpretation.ckbApplicableResistanceEvents();
        //        assertEquals(1, ckbResistanceEvents.size());
        //        assertTrue(ckbResistanceEvents.contains("Event 4 (Treatment 1)"));
    }

    @NotNull
    private static List<TreatmentEvidence> createTestEvidences() {
        List<TreatmentEvidence> evidences = Lists.newArrayList();

        ImmutableTreatmentEvidence.Builder ckbBuilder =
                ImmutableTreatmentEvidence.builder().reported(true).addSources(OrangeInterpreter.CKB_SOURCE);

        // Should be included, all good.
        evidences.add(ckbBuilder.gene(null)
                .event("Event 1")
                .treatment("Treatment 1")
                .onLabel(true)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        // Should be filtered out since it is C-level off-label evidence.
        evidences.add(ckbBuilder.gene(null)
                .event("Event 2")
                .treatment("Treatment 2")
                .onLabel(false)
                .level(EvidenceLevel.C)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        // Should be filtered out as it starts with CDKN2A.
        evidences.add(ckbBuilder.gene("CDKN2A")
                .event("full loss")
                .treatment("Treatment 4")
                .onLabel(true)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        // Should be filtered out as it is VEGFA amp.
        evidences.add(ckbBuilder.gene("VEGFA")
                .event("full gain")
                .treatment("Treatment 2")
                .onLabel(true)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        // Should be filtered since there is no responsive evidence for treatment 3
        evidences.add(ckbBuilder.gene(null)
                .event("Event 3")
                .treatment("Treatment 3")
                .onLabel(false)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESISTANT)
                .build());

        // Should be included, all good.
        evidences.add(ckbBuilder.gene(null)
                .event("Event 4")
                .treatment("Treatment 1")
                .onLabel(false)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESISTANT)
                .build());

        // Should be filtered since evidence level is not high enough
        evidences.add(ckbBuilder.gene(null)
                .event("Event 5")
                .treatment("Treatment 1")
                .onLabel(false)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESISTANT)
                .build());

        // Also have one iclusion
        evidences.add(ImmutableTreatmentEvidence.builder()
                .reported(true)
                .gene(null)
                .event("Event 12")
                .treatment("Trial 1")
                .onLabel(true)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESPONSIVE)
                .addSources(OrangeInterpreter.ICLUSION_SOURCE)
                .build());

        // And 1 ACTIN
        evidences.add(ImmutableTreatmentEvidence.builder()
                .reported(true)
                .gene(null)
                .event("Event 20")
                .treatment("Trial 2")
                .onLabel(true)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESPONSIVE)
                .addSources(OrangeInterpreter.ACTIN_SOURCE)
                .build());

        return evidences;
    }

    @NotNull
    private static OrangeRecord recordWithEvidence(@NotNull List<TreatmentEvidence> evidences) {
        return ImmutableOrangeRecord.builder().from(TestOrangeDataFactory.createMinimalTestOrangeRecord()).evidences(evidences).build();
    }
}