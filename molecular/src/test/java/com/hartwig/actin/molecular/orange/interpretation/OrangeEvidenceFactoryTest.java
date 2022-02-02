package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.MolecularEvidence;
import com.hartwig.actin.molecular.orange.datamodel.EvidenceDirection;
import com.hartwig.actin.molecular.orange.datamodel.EvidenceLevel;
import com.hartwig.actin.molecular.orange.datamodel.ImmutableTreatmentEvidence;
import com.hartwig.actin.molecular.orange.datamodel.TestTreatmentEvidenceFactory;
import com.hartwig.actin.molecular.orange.datamodel.TreatmentEvidence;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class OrangeEvidenceFactoryTest {

    @Test
    public void canCreateMolecularEvidence() {
        List<TreatmentEvidence> evidences = createTestEvidences();

        List<MolecularEvidence> actinTrials = OrangeEvidenceFactory.createActinTrials(evidences);
        assertEquals(1, actinTrials.size());
        assertEquals("B responsive actin event", actinTrials.get(0).event());

        List<MolecularEvidence> externalTrials = OrangeEvidenceFactory.createExternalTrials(evidences);
        assertEquals(1, externalTrials.size());
        assertEquals("B responsive trial event", externalTrials.get(0).event());

        List<MolecularEvidence> approvedResponsiveEvidence =
                OrangeEvidenceFactory.createApprovedResponsiveEvidence(evidences);
        assertEquals(1, approvedResponsiveEvidence.size());
        assertEquals("A on-label responsive event", approvedResponsiveEvidence.get(0).event());

        List<MolecularEvidence> generalExperimentalResponsiveEvidence =
                OrangeEvidenceFactory.createExperimentalResponsiveEvidence(evidences);
        assertEquals(1, generalExperimentalResponsiveEvidence.size());
        assertEquals("A off-label responsive event", generalExperimentalResponsiveEvidence.get(0).event());

        List<MolecularEvidence> generalOtherResponsiveEvidence =
                OrangeEvidenceFactory.createOtherResponsiveEvidence(evidences);
        assertEquals(1, generalOtherResponsiveEvidence.size());
        assertEquals("B off-label responsive event", generalOtherResponsiveEvidence.get(0).event());

        List<MolecularEvidence> generalResistanceEvidence = OrangeEvidenceFactory.createResistanceEvidence(evidences);
        assertEquals(1, generalResistanceEvidence.size());
        assertEquals("A resistant event", generalResistanceEvidence.get(0).event());
    }

    @NotNull
    private static List<TreatmentEvidence> createTestEvidences() {
        List<TreatmentEvidence> evidences = Lists.newArrayList();

        ImmutableTreatmentEvidence.Builder evidenceBuilder = ImmutableTreatmentEvidence.builder()
                .from(TestTreatmentEvidenceFactory.create())
                .reported(true)
                .addSources(OrangeEvidenceFactory.CKB_SOURCE);

        String treatmentWithResponsiveEvidenceA = "treatment";
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
                .treatment(Strings.EMPTY)
                .onLabel(false)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        // Should be other
        evidences.add(evidenceBuilder.gene(null)
                .event("B off-label responsive event")
                .treatment(Strings.EMPTY)
                .onLabel(false)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        // Should be filtered out since it is C-level off-label evidence.
        evidences.add(evidenceBuilder.gene(null)
                .event("C responsive event")
                .treatment(Strings.EMPTY)
                .onLabel(false)
                .level(EvidenceLevel.C)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        // Should be filtered out as it affects a non-applicable gene.
        evidences.add(evidenceBuilder.gene(OrangeEvidenceFactory.NON_APPLICABLE_GENES.iterator().next())
                .event("irrelevant")
                .treatment(Strings.EMPTY)
                .onLabel(true)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        // Should be filtered out as it is a non-applicable event.
        evidences.add(evidenceBuilder.gene(null)
                .event(OrangeEvidenceFactory.NON_APPLICABLE_EVENTS.iterator().next())
                .treatment(Strings.EMPTY)
                .onLabel(true)
                .level(EvidenceLevel.A)
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
        evidences.add(ImmutableTreatmentEvidence.builder()
                .from(TestTreatmentEvidenceFactory.create())
                .reported(true)
                .event("B responsive trial event")
                .onLabel(true)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESPONSIVE)
                .addSources(OrangeEvidenceFactory.ICLUSION_SOURCE)
                .build());

        // And one ACTIN trial that should be included.
        evidences.add(ImmutableTreatmentEvidence.builder()
                .from(TestTreatmentEvidenceFactory.create())
                .reported(true)
                .event("B responsive actin event")
                .onLabel(true)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESPONSIVE)
                .addSources(OrangeEvidenceFactory.ACTIN_SOURCE)
                .build());

        return evidences;
    }
}