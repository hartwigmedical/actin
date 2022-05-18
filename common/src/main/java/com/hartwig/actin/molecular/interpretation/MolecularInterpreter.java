package com.hartwig.actin.molecular.interpretation;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.evidence.EvidenceEntry;
import com.hartwig.actin.molecular.datamodel.evidence.EvidenceType;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;

public final class MolecularInterpreter {

    private MolecularInterpreter() {
    }

    @NotNull
    public static ActionableActinEvents extractActionableEvents(@NotNull MolecularRecord molecular) {
        Set<ActinEvidence> actinEvidence = toActinEvidences(molecular.evidence().actinTrials());
        return ImmutableActionableActinEvents.builder()
                .mutations(extractMutations(actinEvidence))
                .activatedGenes(extractActivatedGenes(actinEvidence))
                .inactivatedGenes(extractInactivatedGenes(actinEvidence))
                .amplifiedGenes(extractAmplifiedGenes(actinEvidence))
                .wildtypeGenes(extractWildtypeGenes(actinEvidence))
                .fusedGenes(extractFusedGenes(actinEvidence))
                .build();
    }

    @NotNull
    private static Set<ActinEvidence> toActinEvidences(@NotNull Set<EvidenceEntry> evidences) {
        Set<ActinEvidence> actinEvidences = Sets.newHashSet();
        for (EvidenceEntry evidence : evidences) {
            actinEvidences.add(ActinEvidence.create(evidence));
        }
        return actinEvidences;
    }

    @NotNull
    private static Set<GeneMutation> extractMutations(@NotNull Set<ActinEvidence> evidences) {
        Set<GeneMutation> geneMutations = Sets.newHashSet();
        for (ActinEvidence evidence : evidences) {
            if (evidence.rule() == EligibilityRule.MUTATION_IN_GENE_X_OF_TYPE_Y) {
                int splitter = evidence.param().indexOf(" ");
                geneMutations.add(ImmutableGeneMutation.builder()
                        .gene(evidence.param().substring(0, splitter))
                        .mutation(evidence.param().substring(splitter + 1))
                        .build());
            }
        }
        return geneMutations;
    }

    @NotNull
    private static Set<String> extractActivatedGenes(@NotNull Set<ActinEvidence> evidences) {
        Set<String> activatedGenes = Sets.newHashSet();
        for (ActinEvidence evidence : evidences) {
            if (evidence.rule() == EligibilityRule.ACTIVATING_MUTATION_IN_GENE_X || (
                    evidence.rule() == EligibilityRule.ACTIVATION_OR_AMPLIFICATION_OF_GENE_X
                            && evidence.type() == EvidenceType.ACTIVATION)) {
                activatedGenes.add(evidence.param());
            }
        }
        return activatedGenes;
    }

    @NotNull
    private static Set<String> extractInactivatedGenes(@NotNull Set<ActinEvidence> evidences) {
        return extractParamsForRule(evidences, EligibilityRule.INACTIVATION_OF_GENE_X);
    }

    @NotNull
    private static Set<String> extractAmplifiedGenes(@NotNull Set<ActinEvidence> evidences) {
        Set<String> amplifiedGenes = Sets.newHashSet();
        for (ActinEvidence evidence : evidences) {
            if (evidence.rule() == EligibilityRule.AMPLIFICATION_OF_GENE_X || (
                    evidence.rule() == EligibilityRule.ACTIVATION_OR_AMPLIFICATION_OF_GENE_X
                            && evidence.type() == EvidenceType.AMPLIFICATION)) {
                amplifiedGenes.add(evidence.param());
            }
        }
        return amplifiedGenes;
    }

    @NotNull
    private static Set<String> extractWildtypeGenes(@NotNull Set<ActinEvidence> evidences) {
        return extractParamsForRule(evidences, EligibilityRule.WILDTYPE_OF_GENE_X);
    }

    @NotNull
    private static Set<String> extractFusedGenes(@NotNull Set<ActinEvidence> evidences) {
        return extractParamsForRule(evidences, EligibilityRule.FUSION_IN_GENE_X);
    }

    @NotNull
    private static Set<String> extractParamsForRule(@NotNull Set<ActinEvidence> evidences, @NotNull EligibilityRule rule) {
        Set<String> params = Sets.newHashSet();
        for (ActinEvidence evidence : evidences) {
            if (evidence.rule() == rule) {
                params.add(evidence.param());
            }
        }
        return params;
    }

    private static class ActinEvidence {

        @NotNull
        private final EligibilityRule rule;
        @NotNull
        private final EvidenceType type;
        @NotNull
        private final String param;

        @NotNull
        static ActinEvidence create(@NotNull EvidenceEntry evidence) {
            String[] parts = evidence.sourceEvent().split(":");
            return new ActinEvidence(EligibilityRule.valueOf(parts[0]), evidence.sourceType(), parts[1].trim());
        }

        private ActinEvidence(@NotNull final EligibilityRule rule, @NotNull final EvidenceType type, @NotNull final String param) {
            this.rule = rule;
            this.type = type;
            this.param = param;
        }

        @NotNull
        public EligibilityRule rule() {
            return rule;
        }

        @NotNull
        public EvidenceType type() {
            return type;
        }

        @NotNull
        public String param() {
            return param;
        }
    }
}
