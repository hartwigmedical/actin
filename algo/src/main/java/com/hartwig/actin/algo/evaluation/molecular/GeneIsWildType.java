package com.hartwig.actin.algo.evaluation.molecular;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.molecular.datamodel.driver.Amplification;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.GeneRole;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect;
import com.hartwig.actin.molecular.datamodel.driver.Variant;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GeneIsWildType implements EvaluationFunction {

    @NotNull
    private final String gene;

    GeneIsWildType(@NotNull final String gene) {
        this.gene = gene;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> reportableEventsWithEffect = Sets.newHashSet();
        Set<String> reportableEventsWithEffectPotentiallyWildtype = Sets.newHashSet();
        Set<String> reportableEventsWithNoEffect = Sets.newHashSet();

        for (Variant variant : record.molecular().drivers().variants()) {
            if (variant.gene().equals(gene) && variant.isReportable()) {
                boolean hasNoEffect =
                        variant.proteinEffect() == ProteinEffect.NO_EFFECT || variant.proteinEffect() == ProteinEffect.NO_EFFECT_PREDICTED;
                if (hasNoEffect) {
                    reportableEventsWithNoEffect.add(variant.event());
                } else {
                    reportableEventsWithEffect.add(variant.event());
                }
            }
        }

        for (Amplification amplification : record.molecular().drivers().amplifications()) {
            if (amplification.gene().equals(gene) && amplification.isReportable() && amplification.geneRole() != GeneRole.TSG) {
                boolean hasNoEffect = amplification.proteinEffect() == ProteinEffect.NO_EFFECT
                        || amplification.proteinEffect() == ProteinEffect.NO_EFFECT_PREDICTED;
                if (hasNoEffect) {
                    reportableEventsWithNoEffect.add(amplification.event());
                } else {
                    reportableEventsWithEffectPotentiallyWildtype.add(amplification.event());
                }
            }
        }

        for (Loss loss : record.molecular().drivers().losses()) {
            if (loss.gene().equals(gene) && loss.isReportable() && loss.geneRole() != GeneRole.ONCO) {
                boolean hasNoEffect =
                        loss.proteinEffect() == ProteinEffect.NO_EFFECT || loss.proteinEffect() == ProteinEffect.NO_EFFECT_PREDICTED;
                if (hasNoEffect) {
                    reportableEventsWithNoEffect.add(loss.event());
                } else {
                    reportableEventsWithEffectPotentiallyWildtype.add(loss.event());
                }
            }
        }

        for (HomozygousDisruption homozygousDisruption : record.molecular().drivers().homozygousDisruptions()) {
            if (homozygousDisruption.gene().equals(gene) && homozygousDisruption.isReportable()
                    && homozygousDisruption.geneRole() != GeneRole.ONCO) {
                boolean hasNoEffect = homozygousDisruption.proteinEffect() == ProteinEffect.NO_EFFECT
                        || homozygousDisruption.proteinEffect() == ProteinEffect.NO_EFFECT_PREDICTED;
                if (hasNoEffect) {
                    reportableEventsWithNoEffect.add(homozygousDisruption.event());
                } else {
                    reportableEventsWithEffect.add(homozygousDisruption.event());
                }
            }
        }

        for (Disruption disruption : record.molecular().drivers().disruptions()) {
            if (disruption.gene().equals(gene) && disruption.isReportable() && disruption.geneRole() != GeneRole.ONCO) {
                boolean hasNoEffect = disruption.proteinEffect() == ProteinEffect.NO_EFFECT
                        || disruption.proteinEffect() == ProteinEffect.NO_EFFECT_PREDICTED;
                if (hasNoEffect) {
                    reportableEventsWithNoEffect.add(disruption.event());
                } else {
                    reportableEventsWithEffect.add(disruption.event());
                }
            }
        }

        for (Fusion fusion : record.molecular().drivers().fusions()) {
            if ((fusion.geneStart().equals(gene) || fusion.geneEnd().equals(gene)) && fusion.isReportable()) {
                boolean hasNoEffect =
                        fusion.proteinEffect() == ProteinEffect.NO_EFFECT || fusion.proteinEffect() == ProteinEffect.NO_EFFECT_PREDICTED;
                if (hasNoEffect) {
                    reportableEventsWithNoEffect.add(fusion.event());
                } else {
                    reportableEventsWithEffect.add(fusion.event());
                }
            }
        }

        if (!reportableEventsWithEffect.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("Gene " + gene + " is not considered wild-type due to " + Format.concat(reportableEventsWithEffect))
                    .addFailGeneralMessages(gene + "not wild-type")
                    .build();
        }

        Evaluation potentialWarnEvaluation =
                evaluatePotentialWarns(reportableEventsWithNoEffect, reportableEventsWithEffectPotentiallyWildtype);

        if (potentialWarnEvaluation != null) {
            return potentialWarnEvaluation;
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.PASS)
                .addPassSpecificMessages("Gene " + gene + " is considered wild-type")
                .addPassGeneralMessages(gene + " wild-type")
                .addInclusionMolecularEvents(gene + "wild-type")
                .build();
    }

    @Nullable
    private Evaluation evaluatePotentialWarns(@NotNull Set<String> reportableEventsWithNoEffect,
            @NotNull Set<String> reportableEventsWithEffectPotentiallyWildtype) {
        Set<String> warnEvents = Sets.newHashSet();
        Set<String> warnSpecificMessages = Sets.newHashSet();
        Set<String> warnGeneralMessages = Sets.newHashSet();

        if (!reportableEventsWithNoEffect.isEmpty()) {
            warnEvents.addAll(reportableEventsWithNoEffect);
            warnSpecificMessages.add("Reportable event(s) in " + gene + " are detected: " + Format.concat(reportableEventsWithNoEffect)
                    + ", however these are annotated with protein effect 'no effect' and thus may potentially be considered wild-type?");
            warnGeneralMessages.add(gene + " potentially wild-type");
        }

        if (!reportableEventsWithEffectPotentiallyWildtype.isEmpty()) {
            warnEvents.addAll(reportableEventsWithEffectPotentiallyWildtype);
            warnSpecificMessages.add(
                    "Reportable event(s) in " + gene + " are detected: " + Format.concat(reportableEventsWithEffectPotentiallyWildtype)
                            + " which may potentially be considered wild-type?");
            warnGeneralMessages.add(gene + " potentially wild-type");
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
