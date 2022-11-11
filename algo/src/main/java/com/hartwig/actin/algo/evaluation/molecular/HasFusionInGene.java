package com.hartwig.actin.algo.evaluation.molecular;

import java.util.EnumSet;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.FusionDriverType;
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HasFusionInGene implements EvaluationFunction {

    @NotNull
    private final String gene;

    HasFusionInGene(@NotNull final String gene) {
        this.gene = gene;
    }

    static final EnumSet<FusionDriverType> ALLOWED_DRIVER_TYPES_FOR_GENE_3 = EnumSet.of(FusionDriverType.KNOWN_PAIR,
            FusionDriverType.KNOWN_PAIR_DEL_DUP,
            FusionDriverType.PROMISCUOUS_BOTH,
            FusionDriverType.PROMISCUOUS_3);

    static final EnumSet<FusionDriverType> ALLOWED_DRIVER_TYPES_FOR_GENE_5 = EnumSet.of(FusionDriverType.KNOWN_PAIR,
            FusionDriverType.KNOWN_PAIR_DEL_DUP,
            FusionDriverType.PROMISCUOUS_BOTH,
            FusionDriverType.PROMISCUOUS_5);

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> matchingFusions = Sets.newHashSet();
        Set<String> fusionsWithNoEffect = Sets.newHashSet();
        Set<String> fusionsWithNoHighDriverLikelihood = Sets.newHashSet();
        Set<String> unreportableFusionsWithGainOfFunction = Sets.newHashSet();

        for (Fusion fusion : record.molecular().drivers().fusions()) {
            boolean isAllowedDriverType = (fusion.geneStart().equals(fusion.geneEnd()) || (fusion.geneStart().equals(gene)
                    && ALLOWED_DRIVER_TYPES_FOR_GENE_5.contains(fusion.driverType())) || (fusion.geneEnd().equals(gene)
                    && ALLOWED_DRIVER_TYPES_FOR_GENE_3.contains(fusion.driverType())));

            if (isAllowedDriverType) {
                if (fusion.isReportable()) {
                    boolean hasNoEffect = fusion.proteinEffect() == ProteinEffect.NO_EFFECT
                            || fusion.proteinEffect() == ProteinEffect.NO_EFFECT_PREDICTED;
                    if (fusion.driverLikelihood() != DriverLikelihood.HIGH) {
                        fusionsWithNoHighDriverLikelihood.add(fusion.event());
                    } else if (hasNoEffect) {
                        fusionsWithNoEffect.add(fusion.event());
                    } else {
                        matchingFusions.add(fusion.event());
                    }
                } else {
                    boolean isGainOfFunction = fusion.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION
                            || fusion.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION_PREDICTED;
                    if (isGainOfFunction) {
                        unreportableFusionsWithGainOfFunction.add(fusion.event());
                    }
                }
            }
        }

        if (!matchingFusions.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addAllInclusionMolecularEvents(matchingFusions)
                    .addPassSpecificMessages("Fusion(s) " + Format.concat(matchingFusions) + " detected in gene " + gene)
                    .addPassGeneralMessages("Fusion(s) detected in gene " + gene)
                    .build();
        }

        Evaluation potentialWarnEvaluation =
                evaluatePotentialWarns(fusionsWithNoEffect, fusionsWithNoHighDriverLikelihood, unreportableFusionsWithGainOfFunction);

        if (potentialWarnEvaluation != null) {
            return potentialWarnEvaluation;
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No fusion detected with gene " + gene)
                .addFailGeneralMessages("Molecular requirements")
                .build();
    }

    @Nullable
    private Evaluation evaluatePotentialWarns(@NotNull Set<String> fusionsWithNoEffect,
            @NotNull Set<String> fusionsWithNoHighDriverLikelihood, @NotNull Set<String> unreportableFusionsWithGainOfFunction) {
        Set<String> warnEvents = Sets.newHashSet();
        Set<String> warnSpecificMessages = Sets.newHashSet();
        Set<String> warnGeneralMessages = Sets.newHashSet();

        if (!fusionsWithNoEffect.isEmpty()) {
            warnEvents.addAll(fusionsWithNoEffect);
            warnSpecificMessages.add("Fusion(s) " + Format.concat(fusionsWithNoEffect) + " detected in gene " + gene
                    + " but annotated as having no protein effect");
            warnGeneralMessages.add("Potential fusion(s) detected in gene " + gene);
        }

        if (!fusionsWithNoHighDriverLikelihood.isEmpty()) {
            warnEvents.addAll(fusionsWithNoHighDriverLikelihood);
            warnSpecificMessages.add("Fusion(s) " + Format.concat(fusionsWithNoHighDriverLikelihood) + " detected in gene " + gene
                    + " but not with high driver likelihood");
            warnGeneralMessages.add("Potential fusion(s) detected in gene " + gene);
        }

        if (!unreportableFusionsWithGainOfFunction.isEmpty()) {
            warnEvents.addAll(unreportableFusionsWithGainOfFunction);
            warnSpecificMessages.add("Fusion(s) " + Format.concat(unreportableFusionsWithGainOfFunction) + " detected in gene " + gene
                    + " but not considered reportable; however fusion is annotated as having gain-of-function");
            warnGeneralMessages.add("Potential fusion(s) detected in gene " + gene);
        }

        if (!warnEvents.isEmpty() && !warnSpecificMessages.isEmpty() && !warnGeneralMessages.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addAllInclusionMolecularEvents(warnEvents)
                    .addAllWarnSpecificMessages(warnSpecificMessages)
                    .addAllWarnGeneralMessages(warnGeneralMessages)
                    .build();
        }

        return null;
    }
}
