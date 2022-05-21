package com.hartwig.actin.molecular.orange.interpretation;

import com.hartwig.actin.molecular.datamodel.evidence.ActinTrialEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableActinTrialEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.MolecularEventType;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectEvidence;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectEvidenceType;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectSource;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ActinTrialEvidenceFactory {

    private static final String TREATMENT_SEPARATOR = "\\|";
    private static final String SOURCE_EVENT_SEPARATOR = ":";

    private ActinTrialEvidenceFactory() {
    }

    @NotNull
    public static ActinTrialEvidence create(@NotNull ProtectEvidence evidence) {
        if (evidence.sources().size() != 1) {
            throw new IllegalStateException(
                    "Number of sources for evidence not equal to 1 when creating actin trial evidence: " + evidence);
        }
        String[] treatmentParts = evidence.treatment().split(TREATMENT_SEPARATOR);
        String trialAcronym = treatmentParts[0];
        String cohortId = null;
        if (treatmentParts.length > 1) {
            cohortId = treatmentParts[1];
        }

        ProtectSource source = evidence.sources().iterator().next();
        String[] sourceEventParts = source.event().split(SOURCE_EVENT_SEPARATOR);
        EligibilityRule rule = EligibilityRule.valueOf(sourceEventParts[0]);
        String param = null;
        if (sourceEventParts.length > 1) {
            param = sourceEventParts[1].trim();
        }

        return ImmutableActinTrialEvidence.builder()
                .trialAcronym(trialAcronym)
                .cohortId(cohortId)
                .event(EvidenceEventExtractor.toEvent(evidence))
                .isInclusionCriterion(evidence.direction().isResponsive())
                .type(extractType(rule, source.type()))
                .gene(extractGene(rule, param))
                .mutation(extractMutation(rule, param))
                .build();
    }

    @NotNull
    private static MolecularEventType extractType(@NotNull EligibilityRule rule, @NotNull ProtectEvidenceType type) {
        switch (rule) {
            case ACTIVATION_OR_AMPLIFICATION_OF_GENE_X: {
                if (type == ProtectEvidenceType.ACTIVATION) {
                    return MolecularEventType.ACTIVATED_GENE;
                } else if (type == ProtectEvidenceType.AMPLIFICATION) {
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
            case HAS_HLA_A_TYPE_X: {
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
            default: {
                return null;
            }
        }
    }

    @Nullable
    private static String extractMutation(@NotNull EligibilityRule rule, @Nullable String param) {
        switch (rule) {
            case MUTATION_IN_GENE_X_OF_TYPE_Y: {
                return param.substring(param.indexOf(" "));
            }
            case HAS_HLA_A_TYPE_X: {
                return param;
            }
        }

        return null;
    }
}
