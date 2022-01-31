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

        List<MolecularEvidence> actinTreatmentEvidence = OrangeEvidenceFactory.createActinTreatmentEvidence(evidences);
        assertEquals(1, actinTreatmentEvidence.size());
        assertEquals("B responsive actin event", actinTreatmentEvidence.get(0).event());

        List<MolecularEvidence> generalTrialEvidence = OrangeEvidenceFactory.createGeneralTrialEvidence(evidences);
        assertEquals(1, generalTrialEvidence.size());
        assertEquals("B responsive trial event", generalTrialEvidence.get(0).event());

        List<MolecularEvidence> generalResponsiveEvidence = OrangeEvidenceFactory.createGeneralResponsiveEvidence(evidences);
        assertEquals(1, generalResponsiveEvidence.size());
        assertEquals("A responsive event", generalResponsiveEvidence.get(0).event());

        List<MolecularEvidence> generalResistanceEvidence = OrangeEvidenceFactory.createGeneralResistanceEvidence(evidences);
        assertEquals(1, generalResistanceEvidence.size());
        assertEquals("A resistant event", generalResistanceEvidence.get(0).event());
    }

    @NotNull
    private static List<TreatmentEvidence> createTestEvidences() {
        List<TreatmentEvidence> evidences = Lists.newArrayList();

        ImmutableTreatmentEvidence.Builder generalEvidenceBuilder = ImmutableTreatmentEvidence.builder()
                .from(TestTreatmentEvidenceFactory.create())
                .reported(true)
                .addSources(OrangeEvidenceFactory.CKB_SOURCE);

        String treatmentWithResponsiveEvidenceA = "treatment";
        // Should be included, all good.
        evidences.add(generalEvidenceBuilder.gene(null)
                .event("A responsive event")
                .treatment(treatmentWithResponsiveEvidenceA)
                .onLabel(true)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        // Should be filtered out since it is C-level off-label evidence.
        evidences.add(generalEvidenceBuilder.gene(null)
                .event("C responsive event")
                .treatment(Strings.EMPTY)
                .onLabel(false)
                .level(EvidenceLevel.C)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        // Should be filtered out as it affects a non-applicable gene.
        evidences.add(generalEvidenceBuilder.gene(OrangeEvidenceFactory.NON_APPLICABLE_GENES.iterator().next())
                .event("irrelevant")
                .treatment(Strings.EMPTY)
                .onLabel(true)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        // Should be filtered out as it is a non-applicable event.
        evidences.add(generalEvidenceBuilder.gene(null)
                .event(OrangeEvidenceFactory.NON_APPLICABLE_EVENTS.iterator().next())
                .treatment(Strings.EMPTY)
                .onLabel(true)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        // Should be filtered since there is no responsive evidence for treatment
        evidences.add(generalEvidenceBuilder.gene(null)
                .event("B resistant event")
                .treatment("Treatment without responsive evidence")
                .onLabel(false)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESISTANT)
                .build());

        // Should be included, all good.
        evidences.add(generalEvidenceBuilder.gene(null)
                .event("A resistant event")
                .treatment(treatmentWithResponsiveEvidenceA)
                .onLabel(false)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESISTANT)
                .build());

        // Should be filtered since evidence level is not as high as the responsive evidence
        evidences.add(generalEvidenceBuilder.gene(null)
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

        // And 1 ACTIN that should be included.
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