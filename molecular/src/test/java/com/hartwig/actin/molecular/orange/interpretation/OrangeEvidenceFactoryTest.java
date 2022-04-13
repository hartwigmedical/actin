package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.evidence.EvidenceEntry;
import com.hartwig.actin.molecular.orange.datamodel.protect.EvidenceDirection;
import com.hartwig.actin.molecular.orange.datamodel.protect.EvidenceLevel;
import com.hartwig.actin.molecular.orange.datamodel.protect.ImmutableProtectEvidence;
import com.hartwig.actin.molecular.orange.datamodel.protect.ImmutableProtectSource;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectEvidence;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectSource;
import com.hartwig.actin.molecular.orange.datamodel.protect.TestProtectDataFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class OrangeEvidenceFactoryTest {

    @Test
    public void canCreateActinTrials() {
        OrangeEvidenceFactory factory = createTestFactory();
        List<ProtectEvidence> evidences = createTestEvidences();

        Set<EvidenceEntry> actinTrials = factory.createActinTrials(evidences);
        assertEquals(1, actinTrials.size());
        assertEquals("B responsive actin event", actinTrials.iterator().next().event());
    }

    @Test
    public void filterActinTrialsWithoutInclusion() {
        OrangeEvidenceFactory factory = new OrangeEvidenceFactory(evidence -> false);
        List<ProtectEvidence> evidences = createTestEvidences();

        assertTrue(factory.createActinTrials(evidences).isEmpty());
    }

    @Test
    public void canCreateExternalTrials() {
        OrangeEvidenceFactory factory = createTestFactory();
        List<ProtectEvidence> evidences = createTestEvidences();

        Set<EvidenceEntry> externalTrials = factory.createExternalTrials(evidences);
        assertEquals(1, externalTrials.size());
        assertEquals("B responsive trial event", externalTrials.iterator().next().event());
    }
    @Test
    public void canCreateApprovedEvidence() {
        OrangeEvidenceFactory factory = createTestFactory();
        List<ProtectEvidence> evidences = createTestEvidences();

        Set<EvidenceEntry> approvedResponsiveEvidence = factory.createApprovedResponsiveEvidence(evidences);
        assertEquals(1, approvedResponsiveEvidence.size());
        assertEquals("A on-label responsive event", approvedResponsiveEvidence.iterator().next().event());
    }

    @Test
    public void canCreateExperimentalResponsiveEvidence() {
        OrangeEvidenceFactory factory = createTestFactory();
        List<ProtectEvidence> evidences = createTestEvidences();

        Set<EvidenceEntry> experimentalResponsiveEvidence = factory.createExperimentalResponsiveEvidence(evidences);
        assertEquals(1, experimentalResponsiveEvidence.size());
        assertEquals("A off-label responsive event", experimentalResponsiveEvidence.iterator().next().event());
    }

    @Test
    public void canCreateOtherResponsiveEvidence() {
        OrangeEvidenceFactory factory = createTestFactory();
        List<ProtectEvidence> evidences = createTestEvidences();

        Set<EvidenceEntry> otherResponsiveEvidence = factory.createOtherResponsiveEvidence(evidences);
        assertEquals(1, otherResponsiveEvidence.size());
        assertEquals("B off-label responsive event", otherResponsiveEvidence.iterator().next().event());
    }

    @Test
    public void canCreateResistanceEvidence() {
        OrangeEvidenceFactory factory = createTestFactory();
        List<ProtectEvidence> evidences = createTestEvidences();

        Set<EvidenceEntry> resistanceEvidence = factory.createResistanceEvidence(evidences);
        assertEquals(1, resistanceEvidence.size());
        assertEquals("A resistant event", resistanceEvidence.iterator().next().event());
    }

    @NotNull
    private static OrangeEvidenceFactory createTestFactory() {
        return new OrangeEvidenceFactory(evidence -> true);
    }

    @NotNull
    private static List<ProtectEvidence> createTestEvidences() {
        List<ProtectEvidence> evidences = Lists.newArrayList();

        ImmutableProtectEvidence.Builder evidenceBuilder = ImmutableProtectEvidence.builder()
                .from(TestProtectDataFactory.create())
                .reported(true)
                .addSources(withName(OrangeEvidenceFactory.CKB_SOURCE));

        String treatmentWithResponsiveEvidenceA = "treatment A on-label";
        // Should be approved treatment
        evidences.add(evidenceBuilder.gene(null)
                .event("A on-label responsive event")
                .treatment(treatmentWithResponsiveEvidenceA)
                .onLabel(true)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        // Should be experimental
        evidences.add(evidenceBuilder.gene(null)
                .event("A off-label responsive event")
                .treatment("treatment A off-label")
                .onLabel(false)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        // Should be other
        evidences.add(evidenceBuilder.gene(null)
                .event("B off-label responsive event")
                .treatment("treatment B off-label")
                .onLabel(false)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        // Should be filtered out since it is C-level off-label evidence.
        evidences.add(evidenceBuilder.gene(null)
                .event("C responsive event")
                .treatment("treatment C off-label")
                .onLabel(false)
                .level(EvidenceLevel.C)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        // Should be filtered since there is no responsive evidence for resistance event
        evidences.add(evidenceBuilder.gene(null)
                .event("B resistant event")
                .treatment("Treatment without responsive evidence")
                .onLabel(false)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESISTANT)
                .build());

        // Should be included in resistance evidence, all good.
        evidences.add(evidenceBuilder.gene(null)
                .event("A resistant event")
                .treatment(treatmentWithResponsiveEvidenceA)
                .onLabel(false)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESISTANT)
                .build());

        // Should be filtered since evidence level is not as high as the responsive evidence
        evidences.add(evidenceBuilder.gene(null)
                .event("B resistant event")
                .treatment(treatmentWithResponsiveEvidenceA)
                .onLabel(false)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESISTANT)
                .build());

        // Add one general trial event that should be included
        evidences.add(ImmutableProtectEvidence.builder()
                .from(TestProtectDataFactory.create())
                .reported(true)
                .event("B responsive trial event")
                .onLabel(true)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESPONSIVE)
                .addSources(withName(OrangeEvidenceFactory.ICLUSION_SOURCE))
                .build());

        // And one ACTIN trial that should be included.
        evidences.add(ImmutableProtectEvidence.builder()
                .from(TestProtectDataFactory.create())
                .reported(true)
                .event("B responsive actin event")
                .onLabel(true)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESPONSIVE)
                .addSources(withName(OrangeEvidenceFactory.ACTIN_SOURCE))
                .build());

        return evidences;
    }

    @NotNull
    private static ProtectSource withName(@NotNull String name) {
        return ImmutableProtectSource.builder().from(TestProtectDataFactory.createSource()).name(name).build();
    }
}