package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.molecular.datamodel.evidence.ActinTrialEvidence;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class EvidenceTestFunctions {

    private EvidenceTestFunctions() {
    }

    public static boolean hasActivatedGene(@NotNull Iterable<ActinTrialEvidence> evidences, @Nullable String geneToFind) {
        boolean hasActivatingMutation = hasEvidence(evidences, EligibilityRule.ACTIVATING_MUTATION_IN_GENE_X, geneToFind, null);
        boolean hasActivationOrAmp = hasEvidence(evidences, EligibilityRule.ACTIVATION_OR_AMPLIFICATION_OF_GENE_X, geneToFind, null);
        return hasActivatingMutation || hasActivationOrAmp;
    }

    public static boolean hasEvidence(@NotNull Iterable<ActinTrialEvidence> evidences, @NotNull EligibilityRule ruleToFind,
            @Nullable String geneToFind, @Nullable String mutationToFind) {
        for (ActinTrialEvidence evidence : evidences) {
            boolean isRuleMatch = evidence.rule() == ruleToFind;
            boolean isGeneMatch = geneToFind == null || geneToFind.equals(evidence.gene());
            boolean isMutationMatch = mutationToFind == null || mutationToFind.equals(evidence.mutation());
            if (isRuleMatch && isGeneMatch && isMutationMatch) {
                return true;
            }
        }
        return false;
    }
}
