package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.hartwig.actin.molecular.datamodel.MolecularEvidence;
import com.hartwig.actin.molecular.orange.datamodel.EvidenceDirection;
import com.hartwig.actin.molecular.orange.datamodel.EvidenceLevel;
import com.hartwig.actin.molecular.orange.datamodel.ImmutableOrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.ImmutableTreatmentEvidence;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeDataFactory;
import com.hartwig.actin.molecular.orange.datamodel.TreatmentEvidence;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class OrangeEvidenceFactoryTest {

    @Test
    public void canCreateMolecularEvidence() {
        OrangeRecord record = recordWithEvidence(createTestEvidences());

        MolecularEvidence evidence = OrangeEvidenceFactory.create(record);

        Multimap<String, String> actinTrialEvidence = evidence.actinTrialEvidence();
        assertEquals(1, actinTrialEvidence.size());
        assertTrue(actinTrialEvidence.keySet().contains("B responsive actin event"));

        Multimap<String, String> generalTrialEvidence = evidence.generalTrialEvidence();
        assertEquals(1, generalTrialEvidence.size());
        assertTrue(generalTrialEvidence.keySet().contains("B responsive trial event"));

        Multimap<String, String> generalResponsiveEvidence = evidence.generalResponsiveEvidence();
        assertEquals(1, generalResponsiveEvidence.size());
        assertTrue(generalResponsiveEvidence.keySet().contains("A responsive event"));

        Multimap<String, String> generalResistanceEvidence = evidence.generalResistanceEvidence();
        assertEquals(1, generalResistanceEvidence.size());
        assertTrue(generalResistanceEvidence.keySet().contains("A resistant event"));
    }

    @NotNull
    private static List<TreatmentEvidence> createTestEvidences() {
        List<TreatmentEvidence> evidences = Lists.newArrayList();

        ImmutableTreatmentEvidence.Builder ckbBuilder = ImmutableTreatmentEvidence.builder()
                .reported(true)
                .addSources(OrangeEvidenceFactory.GENERAL_EVIDENCE_SOURCE)
                .treatment(Strings.EMPTY);

        String treatmentWithResponsiveEvidenceA = "treatment";
        // Should be included, all good.
        evidences.add(ckbBuilder.gene(null).event("A responsive event").treatment(treatmentWithResponsiveEvidenceA)
                .onLabel(true)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESPONSIVE).build());

        // Should be filtered out since it is C-level off-label evidence.
        evidences.add(ckbBuilder.gene(null)
                .event("C responsive event")
                .onLabel(false)
                .level(EvidenceLevel.C)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        // Should be filtered out as it affects a non-applicable gene.
        evidences.add(ckbBuilder.gene(OrangeEvidenceFactory.NON_APPLICABLE_GENES.iterator().next())
                .event("irrelevant")
                .onLabel(true)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        // Should be filtered out as it is a non-applicable event.
        evidences.add(ckbBuilder.gene(null)
                .event(OrangeEvidenceFactory.NON_APPLICABLE_EVENTS.iterator().next())
                .onLabel(true)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        // Should be filtered since there is no responsive evidence for treatment
        evidences.add(ckbBuilder.gene(null).event("B resistant event").treatment("Treatment without responsive evidence").onLabel(false)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESISTANT)
                .build());

        // Should be included, all good.
        evidences.add(ckbBuilder.gene(null).event("A resistant event").treatment(treatmentWithResponsiveEvidenceA)
                .onLabel(false)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESISTANT)
                .build());

        // Should be filtered since evidence level is not as high as the responsive evidence
        evidences.add(ckbBuilder.gene(null).event("B resistant event").treatment(treatmentWithResponsiveEvidenceA)
                .onLabel(false)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESISTANT)
                .build());

        // Add one general trial event that should be included
        evidences.add(ImmutableTreatmentEvidence.builder()
                .reported(true)
                .gene(null)
                .event("B responsive trial event")
                .treatment(Strings.EMPTY)
                .onLabel(true)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESPONSIVE)
                .addSources(OrangeEvidenceFactory.GENERAL_TRIAL_SOURCE)
                .build());

        // And 1 ACTIN that should be included.
        evidences.add(ImmutableTreatmentEvidence.builder()
                .reported(true)
                .gene(null)
                .event("B responsive actin event")
                .treatment(Strings.EMPTY)
                .onLabel(true)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESPONSIVE)
                .addSources(OrangeEvidenceFactory.ACTIN_SOURCE)
                .build());

        return evidences;
    }

    @NotNull
    private static OrangeRecord recordWithEvidence(@NotNull List<TreatmentEvidence> evidences) {
        return ImmutableOrangeRecord.builder().from(TestOrangeDataFactory.createMinimalTestOrangeRecord()).evidences(evidences).build();
    }
}