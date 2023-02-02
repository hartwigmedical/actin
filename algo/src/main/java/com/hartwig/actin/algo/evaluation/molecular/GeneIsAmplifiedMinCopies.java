package com.hartwig.actin.algo.evaluation.molecular;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.molecular.datamodel.driver.CopyNumber;
import com.hartwig.actin.molecular.datamodel.driver.GeneRole;
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO: Merge with GeneIsAmplified
public class GeneIsAmplifiedMinCopies implements EvaluationFunction {

    private static final double SOFT_PLOIDY_FACTOR = 2.5;
    private static final double HARD_PLOIDY_FACTOR = 3;

    @NotNull
    private final String gene;
    private final int requestedMinCopyNumber;

    public GeneIsAmplifiedMinCopies(@NotNull final String gene, final int requestedMinCopyNumber) {
        this.gene = gene;
        this.requestedMinCopyNumber = requestedMinCopyNumber;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Double ploidy = record.molecular().characteristics().ploidy();
        if (ploidy == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("Cannot determine amplification for gene " + gene + " without ploidy")
                    .addFailGeneralMessages("Molecular requirements")
                    .build();
        }

        Set<String> reportableFullAmps = Sets.newHashSet();
        Set<String> reportablePartialAmps = Sets.newHashSet();
        Set<String> ampsWithLossOfFunction = Sets.newHashSet();
        Set<String> ampsOnNonOncogenes = Sets.newHashSet();
        Set<String> ampsThatAreUnreportable = Sets.newHashSet();
        Set<String> ampsThatAreNearCutoff = Sets.newHashSet();
        Set<String> nonAmpsWithSufficientCopyNumber = Sets.newHashSet();

        for (CopyNumber copyNumber : record.molecular().drivers().copyNumbers()) {
            if (copyNumber.gene().equals(gene) && copyNumber.minCopies() >= requestedMinCopyNumber) {
                double relativeMinCopies = copyNumber.minCopies() / ploidy;
                double relativeMaxCopies = copyNumber.maxCopies() / ploidy;

                boolean isAmplification = relativeMaxCopies >= HARD_PLOIDY_FACTOR;
                boolean isNearAmp = relativeMinCopies >= SOFT_PLOIDY_FACTOR && relativeMaxCopies <= HARD_PLOIDY_FACTOR;

                boolean isPotentialOncogene = copyNumber.geneRole() == GeneRole.ONCO || copyNumber.geneRole() == GeneRole.BOTH;
                boolean isLossOfFunction = copyNumber.proteinEffect() == ProteinEffect.LOSS_OF_FUNCTION
                        || copyNumber.proteinEffect() == ProteinEffect.LOSS_OF_FUNCTION_PREDICTED;

                if (isAmplification) {
                    if (!isPotentialOncogene) {
                        ampsOnNonOncogenes.add(copyNumber.event());
                    } else if (isLossOfFunction) {
                        ampsWithLossOfFunction.add(copyNumber.event());
                    } else if (!copyNumber.isReportable()) {
                        ampsThatAreUnreportable.add(copyNumber.event());
                    } else if (relativeMinCopies < HARD_PLOIDY_FACTOR) {
                        reportablePartialAmps.add(copyNumber.event());
                    } else {
                        reportableFullAmps.add(copyNumber.event());
                    }
                } else if (isNearAmp) {
                    ampsThatAreNearCutoff.add(copyNumber.event());
                } else {
                    nonAmpsWithSufficientCopyNumber.add(copyNumber.event());
                }
            }
        }

        if (!reportableFullAmps.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addAllInclusionMolecularEvents(reportableFullAmps)
                    .addPassSpecificMessages(
                            "Amplification detected of gene " + gene + " with min copy number of " + requestedMinCopyNumber)
                    .addPassGeneralMessages(gene + " is amplified with >" + requestedMinCopyNumber + " copies")
                    .build();
        }

        Evaluation potentialWarnEvaluation = evaluatePotentialWarns(reportablePartialAmps,
                ampsWithLossOfFunction,
                ampsOnNonOncogenes,
                ampsThatAreUnreportable,
                ampsThatAreNearCutoff,
                nonAmpsWithSufficientCopyNumber);

        if (potentialWarnEvaluation != null) {
            return potentialWarnEvaluation;
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No amplification detected of gene " + gene + " with min copy number of " + requestedMinCopyNumber)
                .addFailGeneralMessages("Molecular requirements")
                .build();
    }

    @Nullable
    private Evaluation evaluatePotentialWarns(@NotNull Set<String> reportablePartialAmps, @NotNull Set<String> ampsWithLossOfFunction,
            @NotNull Set<String> ampsOnNonOncogenes, @NotNull Set<String> ampsThatAreUnreportable,
            @NotNull Set<String> ampsThatAreNearCutoff, @NotNull Set<String> nonAmpsWithSufficientCopyNumber) {
        Set<String> warnEvents = Sets.newHashSet();
        Set<String> warnSpecificMessages = Sets.newHashSet();
        Set<String> warnGeneralMessages = Sets.newHashSet();

        if (!reportablePartialAmps.isEmpty()) {
            warnEvents.addAll(reportablePartialAmps);
            warnSpecificMessages.add("Gene " + gene + " is partially amplified and not fully amplified");
            warnGeneralMessages.add(gene + " partially amplified");
        }

        if (!ampsWithLossOfFunction.isEmpty()) {
            warnEvents.addAll(ampsWithLossOfFunction);
            warnSpecificMessages.add("Gene " + gene + " is amplified but event is annotated as having loss-of-function impact");
            warnGeneralMessages.add(gene + " amplification with loss-of-function protein impact");
        }

        if (!ampsOnNonOncogenes.isEmpty()) {
            warnEvents.addAll(ampsOnNonOncogenes);
            warnSpecificMessages.add("Gene " + gene + " is amplified but gene " + gene + " is not known as an oncogene");
            warnGeneralMessages.add(gene + " amplification but " + gene + " unknown as oncogene");
        }

        if (!ampsThatAreUnreportable.isEmpty()) {
            warnEvents.addAll(ampsThatAreUnreportable);
            warnSpecificMessages.add("Gene " + gene + " is amplified but not considered reportable");
            warnGeneralMessages.add(gene + " amplification considered non-reportable");
        }

        if (!ampsThatAreNearCutoff.isEmpty()) {
            warnEvents.addAll(ampsThatAreNearCutoff);
            warnSpecificMessages.add("Gene " + gene + " does not meet cut-off for amplification, but is near cut-off");
            warnGeneralMessages.add(gene + " near cut-off for amplification");
        }

        if (!nonAmpsWithSufficientCopyNumber.isEmpty()) {
            warnEvents.addAll(ampsThatAreNearCutoff);
            warnSpecificMessages.add(
                    "Gene " + gene + " does not meet cut-off for amplification, but has copy number > " + requestedMinCopyNumber);
            warnGeneralMessages.add(gene + " sufficient copies but not reported as amplification");
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