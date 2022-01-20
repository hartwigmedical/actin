package com.hartwig.actin.molecular.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.EvidenceDirection;
import com.hartwig.actin.molecular.datamodel.EvidenceLevel;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord;
import com.hartwig.actin.molecular.datamodel.ImmutableTreatmentEvidence;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.TestMolecularDataFactory;
import com.hartwig.actin.molecular.datamodel.TreatmentEvidence;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class MolecularInterpreterTest {

    @Test
    public void canInterpretMolecularRecord() {
        MolecularRecord record = recordWithEvidence(createTestEvidences());

        MolecularInterpretation interpretation = MolecularInterpreter.interpret(record);

        Set<String> eventsWithTrialEligibility = interpretation.eventsWithTrialEligibility();
        assertEquals(1, eventsWithTrialEligibility.size());
        assertTrue(eventsWithTrialEligibility.contains("Event 20"));

        Set<String> iclusionApplicableEvent = interpretation.iclusionApplicableEvents();
        assertEquals(1, iclusionApplicableEvent.size());
        assertTrue(iclusionApplicableEvent.contains("Event 12"));

        Set<String> ckbResponsiveEvents = interpretation.ckbApplicableResponsiveEvents();
        assertEquals(1, ckbResponsiveEvents.size());
        assertTrue(ckbResponsiveEvents.contains("Event 1"));

        Set<String> ckbResistanceEvents = interpretation.ckbApplicableResistanceEvents();
        assertEquals(1, ckbResistanceEvents.size());
        assertTrue(ckbResistanceEvents.contains("Event 4 (Treatment 1)"));
    }

    @NotNull
    private static List<TreatmentEvidence> createTestEvidences() {
        List<TreatmentEvidence> evidences = Lists.newArrayList();

        ImmutableTreatmentEvidence.Builder ckbBuilder = ImmutableTreatmentEvidence.builder().addSources(MolecularInterpreter.CKB_SOURCE);

        // Should be included, all good.
        evidences.add(ckbBuilder.treatment("Treatment 1")
                .direction(EvidenceDirection.RESPONSIVE)
                .level(EvidenceLevel.A)
                .onLabel(true)
                .gene(null)
                .event("Event 1")
                .build());

        // Should be filtered out since it is C-level off-label evidence.
        evidences.add(ckbBuilder.treatment("Treatment 2")
                .direction(EvidenceDirection.RESPONSIVE)
                .level(EvidenceLevel.C)
                .onLabel(false)
                .gene(null)
                .event("Event 2")
                .build());

        // Should be filtered out as it starts with CDKN2A.
        evidences.add(ckbBuilder.treatment("Treatment 4")
                .direction(EvidenceDirection.RESPONSIVE)
                .level(EvidenceLevel.A)
                .onLabel(true)
                .gene("CDKN2A")
                .event("full loss")
                .build());

        // Should be filtered out as it is VEGFA amp.
        evidences.add(ckbBuilder.treatment("Treatment 2")
                .direction(EvidenceDirection.RESPONSIVE)
                .level(EvidenceLevel.A)
                .onLabel(true)
                .gene("VEGFA")
                .event("full gain")
                .build());

        // Should be filtered since there is no responsive evidence for treatment 3
        evidences.add(ckbBuilder.treatment("Treatment 3")
                .direction(EvidenceDirection.RESISTANT)
                .level(EvidenceLevel.B)
                .onLabel(false)
                .gene(null)
                .event("Event 3")
                .build());

        // Should be included, all good.
        evidences.add(ckbBuilder.treatment("Treatment 1")
                .direction(EvidenceDirection.RESISTANT)
                .level(EvidenceLevel.A)
                .onLabel(false)
                .gene(null)
                .event("Event 4")
                .build());

        // Should be filtered since evidence level is not high enough
        evidences.add(ckbBuilder.treatment("Treatment 1")
                .direction(EvidenceDirection.RESISTANT)
                .level(EvidenceLevel.B)
                .onLabel(false)
                .gene(null)
                .event("Event 5")
                .build());

        // Also have one iclusion
        evidences.add(ImmutableTreatmentEvidence.builder()
                .addSources(MolecularInterpreter.ICLUSION_SOURCE)
                .treatment("Trial 1")
                .direction(EvidenceDirection.RESPONSIVE)
                .level(EvidenceLevel.B)
                .onLabel(true)
                .gene(null)
                .event("Event 12")
                .build());

        // And 1 ACTIN
        evidences.add(ImmutableTreatmentEvidence.builder()
                .addSources(MolecularInterpreter.ACTIN_SOURCE)
                .treatment("Trial 2")
                .direction(EvidenceDirection.RESPONSIVE)
                .level(EvidenceLevel.B)
                .onLabel(true)
                .gene(null)
                .event("Event 20")
                .build());

        return evidences;
    }

    @NotNull
    private static MolecularRecord recordWithEvidence(@NotNull List<TreatmentEvidence> evidences) {
        return ImmutableMolecularRecord.builder()
                .from(TestMolecularDataFactory.createMinimalTestMolecularRecord())
                .evidences(evidences)
                .build();
    }
}