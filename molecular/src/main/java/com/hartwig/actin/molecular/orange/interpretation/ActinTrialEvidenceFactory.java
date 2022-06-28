package com.hartwig.actin.molecular.orange.interpretation;

import java.util.Set;

import com.hartwig.actin.molecular.datamodel.evidence.ActinTrialEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableActinTrialEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.MolecularEventType;
import com.hartwig.actin.molecular.orange.datamodel.protect.EvidenceType;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectEvidence;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectSource;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class ActinTrialEvidenceFactory {

    private static final String TREATMENT_SEPARATOR = "\\|";
    private static final String SOURCE_EVENT_SEPARATOR = ":";
    private static final String GENE_MUTATION_SEPARATOR = " ";

    private ActinTrialEvidenceFactory() {
    }

    @NotNull
    public static ActinTrialEvidence create(@NotNull ProtectEvidence evidence) {
        String[] treatmentParts = evidence.treatment().split(TREATMENT_SEPARATOR);
        String trialAcronym = treatmentParts[0];
        String cohortId = null;
        if (treatmentParts.length > 1) {
            cohortId = treatmentParts[1];
        }

        ProtectSource source = findActinSource(evidence.sources());
        String sourceEvent = source.event();

        EligibilityRule rule;
        String param = null;
        if (sourceEvent.contains(SOURCE_EVENT_SEPARATOR)) {
            int position = sourceEvent.indexOf(SOURCE_EVENT_SEPARATOR);
            rule = EligibilityRule.valueOf(sourceEvent.substring(0, position));
            param = sourceEvent.substring(position + 1).trim();
        } else {
            rule = EligibilityRule.valueOf(sourceEvent);
        }

        return ImmutableActinTrialEvidence.builder()
                .trialAcronym(trialAcronym)
                .cohortId(cohortId)
                .event(EvidenceEventExtraction.extract(evidence))
                .isInclusionCriterion(evidence.direction().isResponsive())
                .type(extractType(rule, source.type()))
                .gene(extractGene(rule, param))
                .mutation(extractMutation(rule, param))
                .build();
    }

    @NotNull
    private static ProtectSource findActinSource(@NotNull Set<ProtectSource> sources) {
        for (ProtectSource source : sources) {
            if (source.name().equals(EvidenceConstants.ACTIN_SOURCE)) {
                return source;
            }
        }

        throw new IllegalStateException("Could not find ACTIN source in evidence that is supposedly ACTIN evidence");
    }

    @NotNull
    private static MolecularEventType extractType(@NotNull EligibilityRule rule, @NotNull EvidenceType type) {
        switch (rule) {
            case ACTIVATION_OR_AMPLIFICATION_OF_GENE_X: {
                if (type == EvidenceType.ACTIVATION) {
                    return MolecularEventType.ACTIVATED_GENE;
                } else if (type == EvidenceType.AMPLIFICATION) {
                    return MolecularEventType.AMPLIFIED_GENE;
                } else {
                    throw new IllegalStateException("Invalid evidence type for activation or amplification: " + type);
                }
            }
            case INACTIVATION_OF_GENE_X: {
                return MolecularEventType.INACTIVATED_GENE;
            }
            case ACTIVATING_MUTATION_IN_GENE_X: {
                return MolecularEventType.ACTIVATED_GENE;
            }
            case MUTATION_IN_GENE_X_OF_TYPE_Y: {
                return MolecularEventType.MUTATED_GENE;
            }
            case AMPLIFICATION_OF_GENE_X: {
                return MolecularEventType.AMPLIFIED_GENE;
            }
            case FUSION_IN_GENE_X: {
                return MolecularEventType.FUSED_GENE;
            }
            case WILDTYPE_OF_GENE_X: {
                return MolecularEventType.WILD_TYPE_GENE;
            }
            case MSI_SIGNATURE:
            case HRD_SIGNATURE:
            case TMB_OF_AT_LEAST_X:
            case TML_OF_AT_LEAST_X:
            case TML_OF_AT_MOST_X: {
                return MolecularEventType.SIGNATURE;
            }
            case HAS_HLA_TYPE_X: {
                return MolecularEventType.HLA_ALLELE;
            }
            default: {
                throw new IllegalStateException("Unexpected molecular eligibility rule: " + rule);
            }
        }
    }

    @Nullable
    private static String extractGene(@NotNull EligibilityRule rule, @Nullable String param) {
        switch (rule) {
            case ACTIVATION_OR_AMPLIFICATION_OF_GENE_X:
            case INACTIVATION_OF_GENE_X:
            case ACTIVATING_MUTATION_IN_GENE_X:
            case AMPLIFICATION_OF_GENE_X:
            case FUSION_IN_GENE_X:
            case WILDTYPE_OF_GENE_X: {
                return param;
            }
            case MUTATION_IN_GENE_X_OF_TYPE_Y: {
                return param.substring(0, param.indexOf(GENE_MUTATION_SEPARATOR));
            }
            default: {
                return null;
            }
        }
    }

    @Nullable
    private static String extractMutation(@NotNull EligibilityRule rule, @Nullable String param) {
        switch (rule) {
            case MUTATION_IN_GENE_X_OF_TYPE_Y: {
                return param.substring(param.indexOf(GENE_MUTATION_SEPARATOR) + 1);
            }
            case HAS_HLA_TYPE_X: {
                return param;
            }
        }

        return null;
    }
}
