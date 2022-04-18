package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.orange.datamodel.protect.EvidenceDirection;
import com.hartwig.actin.molecular.orange.datamodel.protect.EvidenceLevel;
import com.hartwig.actin.molecular.orange.datamodel.protect.EvidenceType;
import com.hartwig.actin.molecular.orange.datamodel.protect.ImmutableProtectEvidence;
import com.hartwig.actin.molecular.orange.datamodel.protect.ImmutableProtectSource;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectEvidence;
import com.hartwig.actin.molecular.orange.datamodel.protect.TestProtectDataFactory;
import com.hartwig.actin.serve.datamodel.ImmutableServeRecord;
import com.hartwig.actin.serve.datamodel.ServeRecord;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class OrangeEvidenceEvaluatorTest {

    @Test
    public void ignoreExclusionRecords() {
        ServeRecord inclusion = ImmutableServeRecord.builder()
                .trial(Strings.EMPTY)
                .rule(EligibilityRule.AMPLIFICATION_OF_GENE_X)
                .gene("Y")
                .isUsedAsInclusion(true)
                .build();

        ServeRecord exclusion = ImmutableServeRecord.builder()
                .trial(Strings.EMPTY)
                .rule(EligibilityRule.AMPLIFICATION_OF_GENE_X)
                .gene("X")
                .isUsedAsInclusion(false)
                .build();

        OrangeEvidenceEvaluator evaluator = OrangeEvidenceEvaluator.fromServeRecords(Lists.newArrayList(inclusion, exclusion));

        ProtectEvidence amplificationY = testBuilder(EvidenceType.AMPLIFICATION).gene("Y").build();
        assertTrue(evaluator.isPotentiallyForTrialInclusion(amplificationY));

        ProtectEvidence amplificationX = testBuilder(EvidenceType.AMPLIFICATION).gene("X").build();
        assertFalse(evaluator.isPotentiallyForTrialInclusion(amplificationX));
    }

    @Test
    public void canDetermineInclusionForSignatures() {
        EvidenceEvaluator evaluator = withRecord(withRule(EligibilityRule.TML_OF_AT_LEAST_X));

        ProtectEvidence tmlHigh = testBuilder(EvidenceType.SIGNATURE).event(OrangeEvidenceEvaluator.ORANGE_HIGH_TML).build();
        assertTrue(evaluator.isPotentiallyForTrialInclusion(tmlHigh));

        ProtectEvidence tmbHigh = testBuilder(EvidenceType.SIGNATURE).event(OrangeEvidenceEvaluator.ORANGE_HIGH_TMB).build();
        assertTrue(evaluator.isPotentiallyForTrialInclusion(tmbHigh));

        ProtectEvidence msi = testBuilder(EvidenceType.SIGNATURE).event(OrangeEvidenceEvaluator.ORANGE_MSI).build();
        assertFalse(evaluator.isPotentiallyForTrialInclusion(msi));

        ProtectEvidence hrd = testBuilder(EvidenceType.SIGNATURE).event(OrangeEvidenceEvaluator.ORANGE_HRD).build();
        assertFalse(evaluator.isPotentiallyForTrialInclusion(hrd));
    }

    @Test
    public void canDetermineInclusionForActivation() {
        EvidenceEvaluator evaluator = withRecord(withRuleOnGene(EligibilityRule.ACTIVATION_OR_AMPLIFICATION_OF_GENE_X, "X"));

        ProtectEvidence activation = testBuilder(EvidenceType.ACTIVATION).gene("X").build();
        assertTrue(evaluator.isPotentiallyForTrialInclusion(activation));

        ProtectEvidence inactivation = testBuilder(EvidenceType.INACTIVATION).gene("X").build();
        assertFalse(evaluator.isPotentiallyForTrialInclusion(inactivation));
    }

    @Test
    public void canDetermineInclusionForInactivation() {
        EvidenceEvaluator evaluator = withRecord(withRuleOnGene(EligibilityRule.INACTIVATION_OF_GENE_X, "X"));

        ProtectEvidence inactivation = testBuilder(EvidenceType.INACTIVATION).gene("X").build();
        assertTrue(evaluator.isPotentiallyForTrialInclusion(inactivation));

        ProtectEvidence activation = testBuilder(EvidenceType.ACTIVATION).gene("X").build();
        assertFalse(evaluator.isPotentiallyForTrialInclusion(activation));
    }

    @Test
    public void canDetermineInclusionForAmplification() {
        EvidenceEvaluator evaluator = withRecord(withRuleOnGene(EligibilityRule.AMPLIFICATION_OF_GENE_X, "X"));

        ProtectEvidence amplification = testBuilder(EvidenceType.AMPLIFICATION).gene("X").build();
        assertTrue(evaluator.isPotentiallyForTrialInclusion(amplification));

        ProtectEvidence deletion = testBuilder(EvidenceType.DELETION).gene("X").build();
        assertFalse(evaluator.isPotentiallyForTrialInclusion(deletion));
    }

    @Test
    public void canDetermineInclusionForDeletion() {
        EvidenceEvaluator evaluator = withRecord(withRuleOnGene(EligibilityRule.DELETION_OF_GENE_X, "X"));

        ProtectEvidence deletion = testBuilder(EvidenceType.DELETION).gene("X").build();
        assertTrue(evaluator.isPotentiallyForTrialInclusion(deletion));

        ProtectEvidence amplification = testBuilder(EvidenceType.AMPLIFICATION).gene("X").build();
        assertFalse(evaluator.isPotentiallyForTrialInclusion(amplification));
    }

    @Test
    public void canDetermineInclusionForFusions() {
        EvidenceEvaluator evaluator = withRecord(withRuleOnGene(EligibilityRule.FUSION_IN_GENE_X, "X"));

        ProtectEvidence fusionFive = testBuilder(EvidenceType.PROMISCUOUS_FUSION).gene("X").event("X - Y fusion").build();
        assertTrue(evaluator.isPotentiallyForTrialInclusion(fusionFive));

        ProtectEvidence fusionThree = testBuilder(EvidenceType.FUSION_PAIR).gene("Y").event("Y - X fusion").build();
        assertTrue(evaluator.isPotentiallyForTrialInclusion(fusionThree));
    }

    @Test
    public void canDetermineInclusionForMutations() {
        EvidenceEvaluator mutationEvaluator =
                withRecord(withRuleAndGeneAndMutation(EligibilityRule.MUTATION_IN_GENE_X_OF_TYPE_Y, "X", "Y"));

        ProtectEvidence hotspotCorrect = testBuilder(EvidenceType.HOTSPOT_MUTATION).gene("X").event("Y").build();
        assertTrue(mutationEvaluator.isPotentiallyForTrialInclusion(hotspotCorrect));

        ProtectEvidence hotspotWrong = testBuilder(EvidenceType.HOTSPOT_MUTATION).gene("X").event("Z").build();
        assertFalse(mutationEvaluator.isPotentiallyForTrialInclusion(hotspotWrong));

        ProtectEvidence codon = testBuilder(EvidenceType.CODON_MUTATION).gene("X").event("Y").build();
        assertTrue(mutationEvaluator.isPotentiallyForTrialInclusion(codon));

        ProtectEvidence exon = testBuilder(EvidenceType.EXON_MUTATION).gene("X").event("Y").build();
        assertTrue(mutationEvaluator.isPotentiallyForTrialInclusion(exon));
    }

    @NotNull
    private static ImmutableProtectEvidence.Builder testBuilder(@NotNull EvidenceType type) {
        return ImmutableProtectEvidence.builder()
                .reported(true)
                .event(Strings.EMPTY)
                .treatment(Strings.EMPTY)
                .onLabel(true)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESPONSIVE)
                .addSources(ImmutableProtectSource.builder().from(TestProtectDataFactory.createSource()).type(type).build());
    }

    @NotNull
    private static EvidenceEvaluator withRecord(@NotNull ServeRecord record) {
        return new OrangeEvidenceEvaluator(Lists.newArrayList(record), evidence -> Sets.newHashSet(evidence.event()));
    }

    @NotNull
    private static ServeRecord withRule(@NotNull EligibilityRule rule) {
        return withRuleAndGeneAndMutation(rule, null, null);
    }

    @NotNull
    private static ServeRecord withRuleOnGene(@NotNull EligibilityRule rule, @Nullable String gene) {
        return withRuleAndGeneAndMutation(rule, gene, null);
    }
    
    @NotNull
    private static ServeRecord withRuleAndGeneAndMutation(@NotNull EligibilityRule rule, @Nullable String gene, @Nullable String mutation) {
        return ImmutableServeRecord.builder().trial(Strings.EMPTY).rule(rule).gene(gene).mutation(mutation).isUsedAsInclusion(true).build();
    }
}