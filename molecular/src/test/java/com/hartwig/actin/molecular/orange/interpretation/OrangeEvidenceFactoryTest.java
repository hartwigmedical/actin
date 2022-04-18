package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.evidence.MolecularEvidence;
import com.hartwig.actin.molecular.orange.datamodel.protect.EvidenceDirection;
import com.hartwig.actin.molecular.orange.datamodel.protect.EvidenceLevel;
import com.hartwig.actin.molecular.orange.datamodel.protect.ImmutableProtectEvidence;
import com.hartwig.actin.molecular.orange.datamodel.protect.ImmutableProtectRecord;
import com.hartwig.actin.molecular.orange.datamodel.protect.ImmutableProtectSource;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectEvidence;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectRecord;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectSource;
import com.hartwig.actin.molecular.orange.datamodel.protect.TestProtectDataFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class OrangeEvidenceFactoryTest {

    @Test
    public void canCreateMolecularEvidence() {
        OrangeEvidenceFactory factory = new OrangeEvidenceFactory(evidence -> true);
        ProtectRecord protect = createTestProtectRecord();

        MolecularEvidence evidence = factory.create(protect);

        assertEvidence(evidence);
    }

    @Test
    public void filterActinTrialsWithoutInclusion() {
        OrangeEvidenceFactory factory = new OrangeEvidenceFactory(evidence -> false);
        ProtectRecord protect = createTestProtectRecord();

        assertTrue(factory.create(protect).actinTrials().isEmpty());
    }

    private static void assertEvidence(@NotNull MolecularEvidence evidence) {
        assertEquals(OrangeEvidenceFactory.ACTIN_SOURCE_NAME, evidence.actinSource());
        assertEquals(1, evidence.actinTrials().size());
        assertEquals("B responsive actin event", evidence.actinTrials().iterator().next().event());

        assertEquals(OrangeEvidenceFactory.EXTERNAL_SOURCE_NAME, evidence.externalTrialSource());
        assertEquals(1, evidence.externalTrials().size());
        assertEquals("B responsive external event", evidence.externalTrials().iterator().next().event());

        assertEquals(OrangeEvidenceFactory.EVIDENCE_SOURCE_NAME, evidence.evidenceSource());
        assertEquals(1, evidence.approvedEvidence().size());
        assertEquals("A on-label responsive event", evidence.approvedEvidence().iterator().next().event());

        assertEquals(1, evidence.onLabelExperimentalEvidence().size());
        assertEquals("A off-label responsive event", evidence.onLabelExperimentalEvidence().iterator().next().event());

        assertEquals(1, evidence.offLabelExperimentalEvidence().size());
        assertEquals("B off-label responsive event", evidence.offLabelExperimentalEvidence().iterator().next().event());

        assertEquals(1, evidence.preClinicalEvidence().size());
        assertEquals("D off-label responsive event", evidence.preClinicalEvidence().iterator().next().event());

        assertEquals(1, evidence.knownResistanceEvidence().size());
        assertEquals("A resistant event", evidence.knownResistanceEvidence().iterator().next().event());

        assertEquals(1, evidence.suspectResistanceEvidence().size());
        assertEquals("C resistant event", evidence.suspectResistanceEvidence().iterator().next().event());
    }

    @NotNull
    private static ProtectRecord createTestProtectRecord() {
        Set<ProtectEvidence> evidences = Sets.newHashSet();

        ImmutableProtectEvidence.Builder evidenceBuilder = ImmutableProtectEvidence.builder()
                .from(TestProtectDataFactory.create())
                .reported(true)
                .addSources(withName(OrangeEvidenceFactory.EVIDENCE_SOURCE));

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

        // Should be off-label experimental
        evidences.add(evidenceBuilder.gene(null)
                .event("B off-label responsive event")
                .treatment("treatment B off-label")
                .onLabel(false)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        // Should be pre-clinical since it is D-level off-label evidence.
        String treatmentWithResponsiveEvidenceD = "treatment D off-label";
        evidences.add(evidenceBuilder.gene(null)
                .event("D off-label responsive event")
                .treatment(treatmentWithResponsiveEvidenceD)
                .onLabel(false)
                .level(EvidenceLevel.D)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        // Should be included in known resistance evidence, all good.
        evidences.add(evidenceBuilder.gene(null)
                .event("A resistant event")
                .treatment(treatmentWithResponsiveEvidenceA)
                .onLabel(false)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESISTANT)
                .build());

        // Should be filtered since there is no responsive evidence for resistance event
        evidences.add(evidenceBuilder.gene(null)
                .event("B resistant event")
                .treatment("Treatment without responsive evidence")
                .onLabel(false)
                .level(EvidenceLevel.B)
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

        // Should be included in suspect resistance evidence, all good.
        evidences.add(evidenceBuilder.gene(null)
                .event("C resistant event")
                .treatment(treatmentWithResponsiveEvidenceD)
                .onLabel(true)
                .level(EvidenceLevel.C)
                .direction(EvidenceDirection.RESISTANT)
                .build());

        // Add one general external event that should be included
        evidences.add(ImmutableProtectEvidence.builder()
                .from(TestProtectDataFactory.create())
                .reported(true)
                .event("B responsive external event")
                .onLabel(true)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESPONSIVE)
                .addSources(withName(OrangeEvidenceFactory.EXTERNAL_SOURCE))
                .build());

        // And one ACTIN treatment that should be included.
        evidences.add(ImmutableProtectEvidence.builder()
                .from(TestProtectDataFactory.create())
                .reported(true)
                .event("B responsive actin event")
                .onLabel(true)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESPONSIVE)
                .addSources(withName(OrangeEvidenceFactory.ACTIN_SOURCE))
                .build());

        return ImmutableProtectRecord.builder().evidences(evidences).build();
    }

    @NotNull
    private static ProtectSource withName(@NotNull String name) {
        return ImmutableProtectSource.builder().from(TestProtectDataFactory.createSource()).name(name).build();
    }
}