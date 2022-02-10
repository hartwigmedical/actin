package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.orange.datamodel.EvidenceDirection;
import com.hartwig.actin.molecular.orange.datamodel.EvidenceLevel;
import com.hartwig.actin.molecular.orange.datamodel.EvidenceType;
import com.hartwig.actin.molecular.orange.datamodel.ImmutableTreatmentEvidence;
import com.hartwig.actin.molecular.orange.datamodel.TreatmentEvidence;
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

        TreatmentEvidence amplificationY = createTestBuilder().type(EvidenceType.AMPLIFICATION).gene("Y").build();
        assertTrue(evaluator.isPotentiallyForTrialInclusion(amplificationY));

        TreatmentEvidence amplificationX = createTestBuilder().type(EvidenceType.AMPLIFICATION).gene("X").build();
        assertFalse(evaluator.isPotentiallyForTrialInclusion(amplificationX));
    }

    @Test
    public void canDetermineInclusionForSignatures() {
        EvidenceEvaluator evaluator = withRecord(withRule(EligibilityRule.TML_OF_AT_LEAST_X));

        TreatmentEvidence tmbHigh = createTestBuilder().type(EvidenceType.SIGNATURE).event(OrangeEvidenceEvaluator.ORANGE_HIGH_TMB).build();
        assertTrue(evaluator.isPotentiallyForTrialInclusion(tmbHigh));

        TreatmentEvidence msi = createTestBuilder().type(EvidenceType.SIGNATURE).event(OrangeEvidenceEvaluator.ORANGE_MSI).build();
        assertFalse(evaluator.isPotentiallyForTrialInclusion(msi));

        TreatmentEvidence hrd = createTestBuilder().type(EvidenceType.SIGNATURE).event(OrangeEvidenceEvaluator.ORANGE_HRD).build();
        assertFalse(evaluator.isPotentiallyForTrialInclusion(hrd));
    }

    @Test
    public void canDetermineInclusionForActivation() {
        EvidenceEvaluator evaluator = withRecord(withRuleOnGene(EligibilityRule.ACTIVATION_OR_AMPLIFICATION_OF_GENE_X, "X"));

        TreatmentEvidence activation = createTestBuilder().type(EvidenceType.ACTIVATION).gene("X").build();
        assertTrue(evaluator.isPotentiallyForTrialInclusion(activation));

        TreatmentEvidence inactivation = createTestBuilder().type(EvidenceType.INACTIVATION).gene("X").build();
        assertFalse(evaluator.isPotentiallyForTrialInclusion(inactivation));
    }

    @Test
    public void canDetermineInclusionForInactivation() {
        EvidenceEvaluator evaluator = withRecord(withRuleOnGene(EligibilityRule.INACTIVATION_OF_GENE_X, "X"));

        TreatmentEvidence inactivation = createTestBuilder().type(EvidenceType.INACTIVATION).gene("X").build();
        assertTrue(evaluator.isPotentiallyForTrialInclusion(inactivation));

        TreatmentEvidence activation = createTestBuilder().type(EvidenceType.ACTIVATION).gene("X").build();
        assertFalse(evaluator.isPotentiallyForTrialInclusion(activation));
    }

    @Test
    public void canDetermineInclusionForAmplification() {
        EvidenceEvaluator evaluator = withRecord(withRuleOnGene(EligibilityRule.AMPLIFICATION_OF_GENE_X, "X"));

        TreatmentEvidence amplification = createTestBuilder().type(EvidenceType.AMPLIFICATION).gene("X").build();
        assertTrue(evaluator.isPotentiallyForTrialInclusion(amplification));

        TreatmentEvidence deletion = createTestBuilder().type(EvidenceType.DELETION).gene("X").build();
        assertFalse(evaluator.isPotentiallyForTrialInclusion(deletion));
    }

    @Test
    public void canDetermineInclusionForDeletion() {
        EvidenceEvaluator evaluator = withRecord(withRuleOnGene(EligibilityRule.DELETION_OF_GENE_X, "X"));

        TreatmentEvidence deletion = createTestBuilder().type(EvidenceType.DELETION).gene("X").build();
        assertTrue(evaluator.isPotentiallyForTrialInclusion(deletion));

        TreatmentEvidence amplification = createTestBuilder().type(EvidenceType.AMPLIFICATION).gene("X").build();
        assertFalse(evaluator.isPotentiallyForTrialInclusion(amplification));
    }

    @Test
    public void canDetermineInclusionForFusions() {
        EvidenceEvaluator promiscuousEvaluator = withRecord(withRuleOnGene(EligibilityRule.FUSION_IN_GENE_X, "X"));

        TreatmentEvidence fusionFive = createTestBuilder().type(EvidenceType.PROMISCUOUS_FUSION).gene("X").event("X - Y fusion").build();
        assertTrue(promiscuousEvaluator.isPotentiallyForTrialInclusion(fusionFive));

        TreatmentEvidence fusionThree = createTestBuilder().type(EvidenceType.PROMISCUOUS_FUSION).gene("Y").event("Y - X fusion").build();
        assertTrue(promiscuousEvaluator.isPotentiallyForTrialInclusion(fusionThree));

        EvidenceEvaluator exactEvaluator = withRecord(withRuleAndMutation(EligibilityRule.SPECIFIC_FUSION_OF_X_TO_Y, "X-Y"));

        TreatmentEvidence fusionCorrect = createTestBuilder().type(EvidenceType.FUSION_PAIR).event("X - Y fusion").build();
        assertTrue(exactEvaluator.isPotentiallyForTrialInclusion(fusionCorrect));

        TreatmentEvidence fusionIncorrect = createTestBuilder().type(EvidenceType.FUSION_PAIR).event("Y - X fusion").build();
        assertFalse(exactEvaluator.isPotentiallyForTrialInclusion(fusionIncorrect));
    }

    @Test
    public void canDetermineInclusionForMutations() {
        EvidenceEvaluator mutationEvaluator =
                withRecord(withRuleAndGeneAndMutation(EligibilityRule.MUTATION_IN_GENE_X_OF_TYPE_Y, "X", "Y"));

        TreatmentEvidence hotspotCorrect = createTestBuilder().type(EvidenceType.HOTSPOT_MUTATION).gene("X").event("Y").build();
        assertTrue(mutationEvaluator.isPotentiallyForTrialInclusion(hotspotCorrect));

        TreatmentEvidence hotspotWrong = createTestBuilder().type(EvidenceType.HOTSPOT_MUTATION).gene("X").event("Z").build();
        assertFalse(mutationEvaluator.isPotentiallyForTrialInclusion(hotspotWrong));

        TreatmentEvidence codon = createTestBuilder().type(EvidenceType.CODON_MUTATION).gene("X").event("Y").build();
        assertTrue(mutationEvaluator.isPotentiallyForTrialInclusion(codon));

        TreatmentEvidence exon = createTestBuilder().type(EvidenceType.EXON_MUTATION).gene("X").event("Y").build();
        assertTrue(mutationEvaluator.isPotentiallyForTrialInclusion(exon));
    }

    @NotNull
    private static ImmutableTreatmentEvidence.Builder createTestBuilder() {
        return ImmutableTreatmentEvidence.builder()
                .reported(true)
                .event(Strings.EMPTY)
                .treatment(Strings.EMPTY)
                .onLabel(true)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESPONSIVE);
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
    private static ServeRecord withRuleAndMutation(@NotNull EligibilityRule rule, @Nullable String mutation) {
        return withRuleAndGeneAndMutation(rule, null, mutation);
    }

    @NotNull
    private static ServeRecord withRuleAndGeneAndMutation(@NotNull EligibilityRule rule, @Nullable String gene, @Nullable String mutation) {
        return ImmutableServeRecord.builder().trial(Strings.EMPTY).rule(rule).gene(gene).mutation(mutation).isUsedAsInclusion(true).build();
    }
}