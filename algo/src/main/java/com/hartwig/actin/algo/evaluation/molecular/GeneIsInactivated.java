package com.hartwig.actin.algo.evaluation.molecular;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.molecular.datamodel.driver.GeneRole;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect;
import com.hartwig.actin.molecular.util.MolecularEventFactory;

import org.jetbrains.annotations.NotNull;

public class GeneIsInactivated implements EvaluationFunction {

    @NotNull
    private final String gene;

    GeneIsInactivated(@NotNull final String gene) {
        this.gene = gene;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> inactivationEventsThatQualify = Sets.newHashSet();
        Set<String> inactivationEventsThatAreUnreportable = Sets.newHashSet();
        Set<String> inactivationEventsNoTSG = Sets.newHashSet();
        Set<String> inactivationEventsGainOfFunction = Sets.newHashSet();

        for (HomozygousDisruption homozygousDisruption : record.molecular().drivers().homozygousDisruptions()) {
            if (homozygousDisruption.gene().equals(gene)) {
                String homDisruptionEvent = MolecularEventFactory.homozygousDisruptionEvent(homozygousDisruption);
                boolean isGainOfFunction = homozygousDisruption.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION
                        || homozygousDisruption.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION_PREDICTED;
                if (!homozygousDisruption.isReportable()) {
                    inactivationEventsThatAreUnreportable.add(homDisruptionEvent);
                } else if (homozygousDisruption.geneRole() != GeneRole.TSG) {
                    inactivationEventsNoTSG.add(homDisruptionEvent);
                } else if (isGainOfFunction) {
                    inactivationEventsGainOfFunction.add(homDisruptionEvent);
                } else {
                    inactivationEventsThatQualify.add(homDisruptionEvent);
                }
            }
        }

        for (Loss loss : record.molecular().drivers().losses()) {
            if (loss.gene().equals(gene)) {
                String lossEvent = MolecularEventFactory.lossEvent(loss);
                boolean isGainOfFunction = loss.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION
                        || loss.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION_PREDICTED;
                if (!loss.isReportable()) {
                    inactivationEventsThatAreUnreportable.add(lossEvent);
                } else if (loss.geneRole() != GeneRole.TSG) {
                    inactivationEventsNoTSG.add(lossEvent);
                } else if (isGainOfFunction) {
                    inactivationEventsGainOfFunction.add(lossEvent);
                } else {
                    inactivationEventsThatQualify.add(lossEvent);
                }
            }
        }

        Set<String> reportableNonDriverVariantsWithLossOfFunction = Sets.newHashSet();
        Set<String> reportableHotspotsWithoutLossOfFunction = Sets.newHashSet();
        boolean hasMultipleUnphasedVariants = false;

        if (!inactivationEventsThatQualify.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addAllInclusionMolecularEvents(inactivationEventsThatQualify)
                    .addPassSpecificMessages(
                            "Inactivation events detected for " + gene + ": " + Format.concat(inactivationEventsThatQualify))
                    .addPassGeneralMessages("Inactivation of " + gene)
                    .build();
        }

        if (!inactivationEventsThatAreUnreportable.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addAllInclusionMolecularEvents(inactivationEventsThatAreUnreportable)
                    .addWarnSpecificMessages(
                            "Inactivation events detected for " + gene + ": " + Format.concat(inactivationEventsThatAreUnreportable)
                                    + " but non-reportable")
                    .addWarnGeneralMessages("Potential inactivation of " + gene)
                    .build();
        }

        if (!inactivationEventsNoTSG.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addAllInclusionMolecularEvents(inactivationEventsNoTSG)
                    .addWarnSpecificMessages(
                            "Inactivation events detected for " + gene + ": " + Format.concat(inactivationEventsNoTSG)
                                    + " but not events not annotated as impacting TSG")
                    .addWarnGeneralMessages("Potential inactivation of " + gene)
                    .build();
        }

        if (!inactivationEventsGainOfFunction.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addAllInclusionMolecularEvents(inactivationEventsGainOfFunction)
                    .addWarnSpecificMessages(
                            "Inactivation events detected for " + gene + ": " + Format.concat(inactivationEventsGainOfFunction)
                                    + " but not events annotated as having gain-of-function impact")
                    .addWarnGeneralMessages("Potential inactivation of " + gene)
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No inactivation detected of gene " + gene)
                .addFailGeneralMessages("Molecular requirements")
                .build();
    }
}
