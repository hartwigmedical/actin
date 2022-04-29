package com.hartwig.actin.molecular.orange.interpretation;

import java.util.List;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.mapping.FusionGene;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectEvidence;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectSource;
import com.hartwig.actin.molecular.orange.util.EvidenceFormatter;
import com.hartwig.actin.molecular.orange.util.FusionParser;
import com.hartwig.actin.serve.datamodel.ServeRecord;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

class OrangeEvidenceEvaluator implements EvidenceEvaluator {

    private static final Logger LOGGER = LogManager.getLogger(OrangeEvidenceEvaluator.class);

    static final String ORANGE_HIGH_TML = "High tumor mutation load";
    static final String ORANGE_HIGH_TMB = "High tumor mutation burden";
    static final String ORANGE_MSI = "Microsatellite unstable";
    static final String ORANGE_HRD = "HR deficiency";

    @NotNull
    private final List<ServeRecord> inclusionRecords;
    @NotNull
    private final MutationMapper mutationMapper;

    @NotNull
    public static OrangeEvidenceEvaluator fromServeRecords(@NotNull List<ServeRecord> records) {
        List<ServeRecord> inclusionRecords = Lists.newArrayList();
        for (ServeRecord record : records) {
            if (record.isUsedAsInclusion()) {
                inclusionRecords.add(record);
            }
        }
        return new OrangeEvidenceEvaluator(inclusionRecords, OrangeMutationMapper.fromServeRecords(records));
    }

    @VisibleForTesting
    OrangeEvidenceEvaluator(@NotNull final List<ServeRecord> inclusionRecords, @NotNull final MutationMapper mutationMapper) {
        this.inclusionRecords = inclusionRecords;
        this.mutationMapper = mutationMapper;
    }

    @Override
    public boolean isPotentiallyForTrialInclusion(@NotNull ProtectEvidence evidence) {
        if (evidence.sources().size() != 1) {
            throw new IllegalStateException("Evidence should be filtered down to single source: " + evidence);
        }

        ProtectSource source = evidence.sources().iterator().next();
        switch (source.type()) {
            case VIRAL_PRESENCE: {
                LOGGER.warn("No trial inclusion evaluation is implemented for viral presence: {}", EvidenceFormatter.format(evidence));
                return false;
            }
            case SIGNATURE: {
                return hasInclusiveSignatureRecord(inclusionRecords, evidence.event());
            }
            case ACTIVATION: {
                return hasInclusiveActivationRecord(inclusionRecords, evidence.gene());
            }
            case INACTIVATION: {
                return hasInclusiveInactivationRecord(inclusionRecords, evidence.gene());
            }
            case AMPLIFICATION: {
                return hasInclusiveAmplificationRecord(inclusionRecords, evidence.gene());
            }
            case DELETION: {
                LOGGER.warn("No trial inclusion evaluation is implemented for deletion: {}", EvidenceFormatter.format(evidence));
                return false;
            }
            case PROMISCUOUS_FUSION:
            case FUSION_PAIR: {
                return hasInclusiveFusionRecord(inclusionRecords, evidence.event());
            }
            case HOTSPOT_MUTATION:
            case CODON_MUTATION:
            case EXON_MUTATION: {
                Set<String> mappedMutations = mutationMapper.map(evidence);
                return hasInclusiveMutationRecord(inclusionRecords, evidence.gene(), mappedMutations);
            }
            case ANY_MUTATION: {
                LOGGER.warn("No trial inclusion evaluation is implemented for any mutation: {}", EvidenceFormatter.format(evidence));
                return false;
            }
            default: {
                throw new IllegalArgumentException("Evidence of unrecognized type detected: " + source.type());
            }
        }
    }

    private static boolean hasInclusiveSignatureRecord(@NotNull List<ServeRecord> inclusionRecords, @NotNull String event) {
        switch (event) {
            case ORANGE_HIGH_TMB:
            case ORANGE_HIGH_TML: {
                return hasInclusiveTumorLoadRecord(inclusionRecords);
            }
            case ORANGE_MSI: {
                return hasInclusiveMicrosatelliteRecord(inclusionRecords);
            }
            case ORANGE_HRD: {
                return hasInclusiveHRDeficiencyRecord(inclusionRecords);
            }
            default: {
                throw new IllegalStateException("Unrecognized signature evidence detected: " + event);
            }
        }
    }

    private static boolean hasInclusiveTumorLoadRecord(@NotNull List<ServeRecord> inclusionRecords) {
        return containsRecordWithRule(inclusionRecords, EligibilityRule.TMB_OF_AT_LEAST_X, EligibilityRule.TML_OF_AT_LEAST_X);
    }

    private static boolean hasInclusiveMicrosatelliteRecord(@NotNull List<ServeRecord> inclusionRecords) {
        return containsRecordWithRule(inclusionRecords, EligibilityRule.MSI_SIGNATURE);
    }

    private static boolean hasInclusiveHRDeficiencyRecord(@NotNull List<ServeRecord> inclusionRecords) {
        return containsRecordWithRule(inclusionRecords, EligibilityRule.HRD_SIGNATURE);
    }

    private static boolean hasInclusiveActivationRecord(@NotNull List<ServeRecord> inclusionRecords, @NotNull String gene) {
        return containsRecordWithGeneAndRule(inclusionRecords,
                gene,
                EligibilityRule.ACTIVATION_OR_AMPLIFICATION_OF_GENE_X,
                EligibilityRule.ACTIVATING_MUTATION_IN_GENE_X);
    }

    private static boolean hasInclusiveInactivationRecord(@NotNull List<ServeRecord> inclusionRecords, @NotNull String gene) {
        return containsRecordWithGeneAndRule(inclusionRecords, gene, EligibilityRule.INACTIVATION_OF_GENE_X);
    }

    private static boolean hasInclusiveAmplificationRecord(@NotNull List<ServeRecord> inclusionRecords, @NotNull String gene) {
        return containsRecordWithGeneAndRule(inclusionRecords, gene, EligibilityRule.AMPLIFICATION_OF_GENE_X);
    }


    private static boolean hasInclusiveFusionRecord(@NotNull List<ServeRecord> inclusionRecords, @NotNull String event) {
        FusionGene fusion = FusionParser.fromEvidenceEvent(event);

        boolean hasPromiscuousFive = containsRecordWithGeneAndRule(inclusionRecords, fusion.fiveGene(), EligibilityRule.FUSION_IN_GENE_X);
        boolean hasPromiscuousThree = containsRecordWithGeneAndRule(inclusionRecords, fusion.threeGene(), EligibilityRule.FUSION_IN_GENE_X);

        return hasPromiscuousFive || hasPromiscuousThree;
    }

    private static boolean hasInclusiveMutationRecord(@NotNull List<ServeRecord> inclusionRecords, @NotNull String gene,
            @NotNull Set<String> mappedMutations) {
        for (String mappedMutation : mappedMutations) {
            if (containsRecordWithMutationOnGeneAndRule(inclusionRecords,
                    gene,
                    mappedMutation,
                    EligibilityRule.MUTATION_IN_GENE_X_OF_TYPE_Y)) {
                return true;
            }
        }

        return false;
    }

    private static boolean containsRecordWithMutationOnGeneAndRule(@NotNull List<ServeRecord> records, @NotNull String gene,
            @NotNull String mutation, @NotNull EligibilityRule... rules) {
        Set<EligibilityRule> ruleSet = Sets.newHashSet(rules);
        for (ServeRecord record : records) {
            if (gene.equals(record.gene()) && mutation.equals(record.mutation()) && ruleSet.contains(record.rule())) {
                return true;
            }
        }

        return false;
    }

    private static boolean containsRecordWithGeneAndRule(@NotNull List<ServeRecord> records, @NotNull String gene,
            @NotNull EligibilityRule... rules) {
        Set<EligibilityRule> ruleSet = Sets.newHashSet(rules);
        for (ServeRecord record : records) {
            if (gene.equals(record.gene()) && ruleSet.contains(record.rule())) {
                return true;
            }
        }

        return false;
    }

    private static boolean containsRecordWithRule(@NotNull List<ServeRecord> records, @NotNull EligibilityRule... rules) {
        Set<EligibilityRule> ruleSet = Sets.newHashSet(rules);
        for (ServeRecord record : records) {
            if (ruleSet.contains(record.rule())) {
                return true;
            }
        }

        return false;
    }
}
