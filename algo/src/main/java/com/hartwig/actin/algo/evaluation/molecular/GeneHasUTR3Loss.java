package com.hartwig.actin.algo.evaluation.molecular;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.molecular.datamodel.driver.CodingContext;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.RegionType;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.driver.VariantEffect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GeneHasUTR3Loss implements EvaluationFunction {

    @NotNull
    private final String gene;

    GeneHasUTR3Loss(@NotNull final String gene) {
        this.gene = gene;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> hotspotsIn3UTR = Sets.newHashSet();
        Set<String> hotspotsIn3UTRUnreportable = Sets.newHashSet();
        Set<String> vusIn3UTR = Sets.newHashSet();

        for (Variant variant : record.molecular().drivers().variants()) {
            if (variant.gene().equals(gene) && variant.canonicalImpact().effects().contains(VariantEffect.THREE_PRIME_UTR)) {
                if (variant.isHotspot() && variant.isReportable()) {
                    hotspotsIn3UTR.add(variant.event());
                } else if (variant.isHotspot()) {
                    hotspotsIn3UTRUnreportable.add(variant.event());
                } else {
                    vusIn3UTR.add(variant.event());
                }
            }
        }

        Set<String> disruptionsIn3UTR = Sets.newHashSet();
        for (Disruption disruption : record.molecular().drivers().disruptions()) {
            if (disruption.gene().equals(gene) && disruption.codingContext() == CodingContext.UTR_3P
                    && disruption.regionType() == RegionType.EXONIC) {
                disruptionsIn3UTR.add(disruption.event());
            }
        }

        if (!hotspotsIn3UTR.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addAllInclusionMolecularEvents(hotspotsIn3UTR)
                    .addPassSpecificMessages(
                            "3' UTR hotspot mutation(s) in " + gene + " should lead to 3' UTR loss: " + Format.concat(hotspotsIn3UTR))
                    .addPassGeneralMessages("3' UTR loss of " + gene)
                    .build();
        }

        Evaluation potentialWarnEvaluation = evaluatePotentialWarns(hotspotsIn3UTRUnreportable, vusIn3UTR, disruptionsIn3UTR);

        if (potentialWarnEvaluation != null) {
            return potentialWarnEvaluation;
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No variants detected in 3' UTR region of " + gene)
                .addFailGeneralMessages("No 3' UTR loss of " + gene)
                .build();
    }

    @Nullable
    private Evaluation evaluatePotentialWarns(@NotNull Set<String> vusIn3UTR, @NotNull Set<String> hotspotsIn3UTRUnreportable,
            @NotNull Set<String> disruptionsIn3UTR) {
        Set<String> warnEvents = Sets.newHashSet();
        Set<String> warnSpecificMessages = Sets.newHashSet();
        Set<String> warnGeneralMessages = Sets.newHashSet();

        if (!hotspotsIn3UTRUnreportable.isEmpty()) {
            warnEvents.addAll(hotspotsIn3UTRUnreportable);
            warnSpecificMessages.add(
                    "Hotspot mutation detected in 3' UTR region of " + gene + " which may lead to 3' UTR loss: "
                            + Format.concat(hotspotsIn3UTRUnreportable) + " but variant is not considered reportable");
            warnGeneralMessages.add("Potential 3' UTR loss of " + gene);
        }

        if (!vusIn3UTR.isEmpty()) {
            warnEvents.addAll(vusIn3UTR);
            warnSpecificMessages.add(
                    "VUS mutation detected in 3' UTR region of " + gene + " which may lead to 3' UTR loss: " + Format.concat(vusIn3UTR));
            warnGeneralMessages.add("Potential 3' UTR loss of " + gene);
        }

        if (!disruptionsIn3UTR.isEmpty()) {
            warnEvents.addAll(disruptionsIn3UTR);
            warnSpecificMessages.add(
                    "Disruption(s) detected in 3' UTR region of " + gene + " which may lead to 3' UTR loss: " + Format.concat(
                            disruptionsIn3UTR));
            warnGeneralMessages.add("Potential 3' UTR loss of " + gene);
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
