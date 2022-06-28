package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.evidence.ActinTrialEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.MolecularEventType;
import com.hartwig.actin.molecular.orange.datamodel.protect.EvidenceType;
import com.hartwig.actin.molecular.orange.datamodel.protect.ImmutableProtectSource;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectEvidence;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectTestFactory;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class ActinTrialEvidenceFactoryTest {

    @Test
    public void canParseTreatments() {
        ActinTrialEvidence evidence1 = ActinTrialEvidenceFactory.create(withTreatment("Trial 1|A"));
        assertEquals("Trial 1", evidence1.trialAcronym());
        assertEquals("A", evidence1.cohortId());

        ActinTrialEvidence evidence2 = ActinTrialEvidenceFactory.create(withTreatment("Trial 2"));
        assertEquals("Trial 2", evidence2.trialAcronym());
        assertNull(evidence2.cohortId());
    }

    @Test
    public void canCreateActivatingMutation() {
        ProtectEvidence evidence = create(EligibilityRule.ACTIVATING_MUTATION_IN_GENE_X + ": ABL1", EvidenceType.ACTIVATION);
        ActinTrialEvidence activation = ActinTrialEvidenceFactory.create(evidence);
        assertEquals(MolecularEventType.ACTIVATED_GENE, activation.type());
        assertEquals("ABL1", activation.gene());
        assertNull(activation.mutation());
    }

    @Test
    public void canCreateActivationOrAmplificationAct() {
        ProtectEvidence evidence = create(EligibilityRule.ACTIVATION_OR_AMPLIFICATION_OF_GENE_X + ": EGFR", EvidenceType.ACTIVATION);
        ActinTrialEvidence activation = ActinTrialEvidenceFactory.create(evidence);
        assertEquals(MolecularEventType.ACTIVATED_GENE, activation.type());
        assertEquals("EGFR", activation.gene());
        assertNull(activation.mutation());
    }

    @Test
    public void canCreateActivationOrAmplificationAmp() {
        ProtectEvidence evidence = create(EligibilityRule.ACTIVATION_OR_AMPLIFICATION_OF_GENE_X + ": MYC", EvidenceType.AMPLIFICATION);
        ActinTrialEvidence amplification = ActinTrialEvidenceFactory.create(evidence);
        assertEquals(MolecularEventType.AMPLIFIED_GENE, amplification.type());
        assertEquals("MYC", amplification.gene());
        assertNull(amplification.mutation());
    }

    @Test
    public void canCreateAmplification() {
        ProtectEvidence evidence = create(EligibilityRule.AMPLIFICATION_OF_GENE_X + ": CCND1", EvidenceType.AMPLIFICATION);
        ActinTrialEvidence amplification = ActinTrialEvidenceFactory.create(evidence);
        assertEquals(MolecularEventType.AMPLIFIED_GENE, amplification.type());
        assertEquals("CCND1", amplification.gene());
        assertNull(amplification.mutation());
    }

    @Test
    public void canCreateFusion() {
        ProtectEvidence evidence = create(EligibilityRule.FUSION_IN_GENE_X + ": ALK", EvidenceType.PROMISCUOUS_FUSION);
        ActinTrialEvidence fusion = ActinTrialEvidenceFactory.create(evidence);
        assertEquals(MolecularEventType.FUSED_GENE, fusion.type());
        assertEquals("ALK", fusion.gene());
        assertNull(fusion.mutation());
    }

    @Test
    public void canCreateWildType() {
        ProtectEvidence evidence = create(EligibilityRule.WILDTYPE_OF_GENE_X + ": KRAS", EvidenceType.WILD_TYPE);
        ActinTrialEvidence wildType = ActinTrialEvidenceFactory.create(evidence);
        assertEquals(MolecularEventType.WILD_TYPE_GENE, wildType.type());
        assertEquals("KRAS", wildType.gene());
        assertNull(wildType.mutation());
    }

    @Test
    public void canCreateInactivation() {
        ProtectEvidence evidence = create(EligibilityRule.INACTIVATION_OF_GENE_X + ": BAP1", EvidenceType.INACTIVATION);
        ActinTrialEvidence inactivation = ActinTrialEvidenceFactory.create(evidence);
        assertEquals(MolecularEventType.INACTIVATED_GENE, inactivation.type());
        assertEquals("BAP1", inactivation.gene());
        assertNull(inactivation.mutation());
    }

    @Test
    public void canCreateMutations() {
        ProtectEvidence evidence1 = create(EligibilityRule.MUTATION_IN_GENE_X_OF_TYPE_Y + ": BRAF V600E", EvidenceType.HOTSPOT_MUTATION);
        ActinTrialEvidence hotspot = ActinTrialEvidenceFactory.create(evidence1);
        assertEquals(MolecularEventType.MUTATED_GENE, hotspot.type());
        assertEquals("BRAF", hotspot.gene());
        assertEquals("V600E", hotspot.mutation());

        ProtectEvidence evidence2 = create(EligibilityRule.MUTATION_IN_GENE_X_OF_TYPE_Y + ": KRAS exon 2-4", EvidenceType.EXON_MUTATION);
        ActinTrialEvidence exon = ActinTrialEvidenceFactory.create(evidence2);
        assertEquals(MolecularEventType.MUTATED_GENE, exon.type());
        assertEquals("KRAS", exon.gene());
        assertEquals("exon 2-4", exon.mutation());
    }

    @Test
    public void canCreateSignatures() {
        Set<EligibilityRule> signatures = Sets.newHashSet(EligibilityRule.MSI_SIGNATURE,
                EligibilityRule.HRD_SIGNATURE,
                EligibilityRule.TMB_OF_AT_LEAST_X,
                EligibilityRule.TML_OF_AT_LEAST_X,
                EligibilityRule.TML_OF_AT_MOST_X);

        for (EligibilityRule signature : signatures) {
            assertEquals(MolecularEventType.SIGNATURE, ActinTrialEvidenceFactory.create(create(signature.toString())).type());
        }
    }

    @Test
    public void canCreateHLA() {
        ProtectEvidence evidence1 = create(EligibilityRule.HAS_HLA_TYPE_X + ": A*02:01");
        ActinTrialEvidence hla = ActinTrialEvidenceFactory.create(evidence1);
        assertEquals(MolecularEventType.HLA_ALLELE, hla.type());
        assertNull(hla.gene());
        assertEquals("A*02:01", hla.mutation());
    }

    @NotNull
    private static ProtectEvidence create(@NotNull String sourceEvent) {
        return create(sourceEvent, EvidenceType.ANY_MUTATION);
    }

    @NotNull
    private static ProtectEvidence create(@NotNull String sourceEvent, @NotNull EvidenceType type) {
        return ProtectTestFactory.builder().addSources(actinSourceBuilder().event(sourceEvent).type(type).build()).build();
    }

    @NotNull
    private static ProtectEvidence withTreatment(@NotNull String treatment) {
        return ProtectTestFactory.builder()
                .treatment(treatment)
                .addSources(actinSourceBuilder().event(EligibilityRule.HRD_SIGNATURE.toString()).build())
                .build();
    }

    @NotNull
    private static ImmutableProtectSource.Builder actinSourceBuilder() {
        return ProtectTestFactory.sourceBuilder().name(EvidenceConstants.ACTIN_SOURCE);
    }
}