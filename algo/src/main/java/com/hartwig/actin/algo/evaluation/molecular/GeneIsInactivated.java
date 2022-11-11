package com.hartwig.actin.algo.evaluation.molecular;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.molecular.datamodel.driver.CodingEffect;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.GeneRole;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect;
import com.hartwig.actin.molecular.datamodel.driver.Variant;

import org.jetbrains.annotations.NotNull;

public class GeneIsInactivated implements EvaluationFunction {

    static final Set<CodingEffect> INACTIVATING_CODING_EFFECTS = Sets.newHashSet();

    static {
        INACTIVATING_CODING_EFFECTS.add(CodingEffect.NONSENSE_OR_FRAMESHIFT);
        INACTIVATING_CODING_EFFECTS.add(CodingEffect.MISSENSE);
        INACTIVATING_CODING_EFFECTS.add(CodingEffect.SPLICE);
    }

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
                boolean isGainOfFunction = homozygousDisruption.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION
                        || homozygousDisruption.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION_PREDICTED;
                if (!homozygousDisruption.isReportable()) {
                    inactivationEventsThatAreUnreportable.add(homozygousDisruption.event());
                } else if (homozygousDisruption.geneRole() == GeneRole.ONCO || homozygousDisruption.geneRole() == GeneRole.UNKNOWN) {
                    inactivationEventsNoTSG.add(homozygousDisruption.event());
                } else if (isGainOfFunction) {
                    inactivationEventsGainOfFunction.add(homozygousDisruption.event());
                } else {
                    inactivationEventsThatQualify.add(homozygousDisruption.event());
                }
            }
        }

        for (Loss loss : record.molecular().drivers().losses()) {
            if (loss.gene().equals(gene)) {
                boolean isGainOfFunction = loss.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION
                        || loss.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION_PREDICTED;
                if (!loss.isReportable()) {
                    inactivationEventsThatAreUnreportable.add(loss.event());
                } else if (loss.geneRole() == GeneRole.ONCO || loss.geneRole() == GeneRole.UNKNOWN) {
                    inactivationEventsNoTSG.add(loss.event());
                } else if (isGainOfFunction) {
                    inactivationEventsGainOfFunction.add(loss.event());
                } else {
                    inactivationEventsThatQualify.add(loss.event());
                }
            }
        }

        Set<String> reportableNonDriverVariantsWithLossOfFunction = Sets.newHashSet();
        Set<String> eventsThatMayBeUnphased = Sets.newHashSet();
        Set<Integer> evaluatedPhaseGroups = Sets.newHashSet();

        for (Variant variant : record.molecular().drivers().variants()) {
            if (variant.gene().equals(gene) && INACTIVATING_CODING_EFFECTS.contains(variant.canonicalImpact().codingEffect())) {
                if (!variant.isReportable()) {
                    inactivationEventsThatAreUnreportable.add(variant.event());
                } else {
                    Integer phaseGroup = null;
                    if (phaseGroup == null || !evaluatedPhaseGroups.contains(phaseGroup)) {
                        evaluatedPhaseGroups.add(phaseGroup);
                        eventsThatMayBeUnphased.add(variant.event());
                    }

                    boolean isLossOfFunction = variant.proteinEffect() == ProteinEffect.LOSS_OF_FUNCTION
                            || variant.proteinEffect() == ProteinEffect.LOSS_OF_FUNCTION_PREDICTED;

                    boolean isHighDriver = variant.driverLikelihood() == DriverLikelihood.HIGH;
                    if (isHighDriver && variant.isBiallelic()) {
                        boolean isGainOfFunction = variant.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION
                                || variant.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION_PREDICTED;

                        if (variant.geneRole() == GeneRole.ONCO || variant.geneRole() == GeneRole.UNKNOWN) {
                            inactivationEventsNoTSG.add(variant.event());
                        } else if (isGainOfFunction) {
                            inactivationEventsGainOfFunction.add(variant.event());
                        } else {
                            inactivationEventsThatQualify.add(variant.event());
                        }
                    } else if (isLossOfFunction) {
                        reportableNonDriverVariantsWithLossOfFunction.add(variant.event());
                    }
                }
            }
        }

        Set<Integer> evaluatedClusterGroups = Sets.newHashSet();
        for (Disruption disruption : record.molecular().drivers().disruptions()) {
            if (disruption.gene().equals(gene) && disruption.isReportable()) {
                Integer clusterGroup = disruption.clusterGroup();
                if (clusterGroup == null || !evaluatedClusterGroups.contains(clusterGroup)) {
                    evaluatedClusterGroups.add(clusterGroup);
                    eventsThatMayBeUnphased.add(disruption.event());
                }
            }
        }

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
                                    + " but considered non-reportable")
                    .addWarnGeneralMessages("Potential inactivation of " + gene)
                    .build();
        }

        if (!inactivationEventsNoTSG.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addAllInclusionMolecularEvents(inactivationEventsNoTSG)
                    .addWarnSpecificMessages("Inactivation events detected for " + gene + ": " + Format.concat(inactivationEventsNoTSG)
                            + " but gene is not annotated with function TSG")
                    .addWarnGeneralMessages("Potential inactivation of " + gene)
                    .build();
        }

        if (!inactivationEventsGainOfFunction.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addAllInclusionMolecularEvents(inactivationEventsGainOfFunction)
                    .addWarnSpecificMessages(
                            "Inactivation events detected for " + gene + ": " + Format.concat(inactivationEventsGainOfFunction)
                                    + " but no events annotated as having gain-of-function impact")
                    .addWarnGeneralMessages("Potential inactivation of " + gene)
                    .build();
        }

        if (!reportableNonDriverVariantsWithLossOfFunction.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addAllInclusionMolecularEvents(reportableNonDriverVariantsWithLossOfFunction)
                    .addWarnSpecificMessages(
                            "Inactivation events detected for " + gene + ": " + Format.concat(reportableNonDriverVariantsWithLossOfFunction)
                                    + " but events are low-driver yet annotated with loss-of-function")
                    .addWarnGeneralMessages("Potential inactivation of " + gene)
                    .build();
        }

        if (!eventsThatMayBeUnphased.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addAllInclusionMolecularEvents(eventsThatMayBeUnphased)
                    .addWarnSpecificMessages("Multiple events detected for " + gene + ": " + Format.concat(eventsThatMayBeUnphased)
                            + " that may together potentially inactivate the gene")
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
