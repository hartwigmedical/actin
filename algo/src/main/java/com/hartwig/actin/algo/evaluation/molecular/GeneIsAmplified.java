package com.hartwig.actin.algo.evaluation.molecular;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.molecular.datamodel.driver.Amplification;
import com.hartwig.actin.molecular.datamodel.driver.GeneRole;
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect;

import org.jetbrains.annotations.NotNull;

public class GeneIsAmplified implements EvaluationFunction {

    private static final double SOFT_PLOIDY_FACTOR = 2.5;
    private static final double HARD_PLOIDY_FACTOR = 3;

    @NotNull
    private final String gene;

    GeneIsAmplified(@NotNull final String gene) {
        this.gene = gene;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Double ploidy = record.molecular().characteristics().ploidy();
        if (ploidy == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("Cannot determine amplification for " + gene + " without ploidy")
                    .addFailGeneralMessages("Molecular requirements")
                    .build();
        }

        Set<String> reportableFullAmps = Sets.newHashSet();
        Set<String> reportablePartialAmps = Sets.newHashSet();
        Set<String> ampsWithLossOfFunction = Sets.newHashSet();
        Set<String> ampsOnNonOncogenes = Sets.newHashSet();
        Set<String> ampsThatAreUnreportable = Sets.newHashSet();
        Set<String> ampsThatAreNearCutoff = Sets.newHashSet();

        for (Amplification amplification : record.molecular().drivers().amplifications()) {
            if (amplification.gene().equals(gene)) {
                double relativeMinCopies = amplification.minCopies() / ploidy;
                double relativeMaxCopies = amplification.maxCopies() / ploidy;

                boolean isFullAmp = relativeMinCopies >= HARD_PLOIDY_FACTOR && relativeMaxCopies >= HARD_PLOIDY_FACTOR;
                boolean isPartialAmp = relativeMinCopies <= HARD_PLOIDY_FACTOR && relativeMaxCopies >= HARD_PLOIDY_FACTOR;
                boolean isAmp = isFullAmp || isPartialAmp;
                boolean isNearAmp = relativeMinCopies >= SOFT_PLOIDY_FACTOR && relativeMaxCopies <= HARD_PLOIDY_FACTOR;

                boolean isPotentialOncogene = amplification.geneRole() == GeneRole.ONCO || amplification.geneRole() == GeneRole.BOTH;
                boolean isLossOfFunction = amplification.proteinEffect() == ProteinEffect.LOSS_OF_FUNCTION
                        || amplification.proteinEffect() == ProteinEffect.LOSS_OF_FUNCTION_PREDICTED;

                if (isAmp) {
                    if (!isPotentialOncogene) {
                        ampsOnNonOncogenes.add(amplification.event());
                    } else if (isLossOfFunction) {
                        ampsWithLossOfFunction.add(amplification.event());
                    } else if (!amplification.isReportable()) {
                        ampsThatAreUnreportable.add(amplification.event());
                    } else if (isPartialAmp) {
                        reportablePartialAmps.add(amplification.event());
                    } else {
                        reportableFullAmps.add(amplification.event());
                    }
                } else if (isNearAmp) {
                    ampsThatAreNearCutoff.add(amplification.event());
                }
            }
        }

        if (!reportableFullAmps.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addAllInclusionMolecularEvents(reportableFullAmps)
                    .addPassSpecificMessages(gene + " is fully amplified")
                    .addPassGeneralMessages(gene + " is fully amplified")
                    .build();
        }

        if (!reportablePartialAmps.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addAllInclusionMolecularEvents(reportablePartialAmps)
                    .addWarnSpecificMessages(gene + " is partially amplified")
                    .addWarnGeneralMessages(gene + " is partially amplified")
                    .build();
        }

        if (!ampsWithLossOfFunction.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addAllInclusionMolecularEvents(ampsWithLossOfFunction)
                    .addWarnSpecificMessages(gene + " is amplified but considered having loss-of-function impact")
                    .addWarnGeneralMessages("Potential " + gene + " amplification")
                    .build();
        }

        if (!ampsOnNonOncogenes.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addAllInclusionMolecularEvents(ampsOnNonOncogenes)
                    .addWarnSpecificMessages(gene + " is amplified but not known as an oncogene")
                    .addWarnGeneralMessages("Potential " + gene + " amplification")
                    .build();
        }

        if (!ampsThatAreUnreportable.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addAllInclusionMolecularEvents(ampsThatAreUnreportable)
                    .addWarnSpecificMessages(gene + " is amplified but not considered reportable")
                    .addWarnGeneralMessages("Potential " + gene + " amplification")
                    .build();
        }

        if (!ampsThatAreNearCutoff.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addAllInclusionMolecularEvents(ampsThatAreNearCutoff)
                    .addWarnSpecificMessages(gene + " is near-amplified")
                    .addWarnGeneralMessages("Potential " + gene + " amplification")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No amplification detected of gene " + gene)
                .addFailGeneralMessages("Molecular requirements")
                .build();
    }
}
