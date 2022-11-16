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
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.GeneRole;
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect;
import com.hartwig.actin.molecular.datamodel.driver.Variant;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GeneHasActivatingMutation implements EvaluationFunction {

    @NotNull
    private final String gene;

    GeneHasActivatingMutation(@NotNull final String gene) {
        this.gene = gene;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> activatingVariants = Sets.newHashSet();
        Set<String> activatingVariantsAssociatedWithResistance = Sets.newHashSet();
        Set<String> activatingVariantsWithNoGainOfFunction = Sets.newHashSet();
        Set<String> activatingVariantsInNonOncogene = Sets.newHashSet();
        Set<String> nonHighDriverGainOfFunctionVariants = Sets.newHashSet();
        Set<String> nonHighDriverVariants = Sets.newHashSet();
        Set<String> unreportableMissenseOrHotspotVariants = Sets.newHashSet();

        for (Variant variant : record.molecular().drivers().variants()) {
            if (variant.gene().equals(gene)) {
                boolean isGainOfFunction = variant.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION
                        || variant.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION_PREDICTED;
                boolean isPotentialOncogene = variant.geneRole() == GeneRole.ONCO || variant.geneRole() == GeneRole.BOTH;

                if (variant.isReportable()) {
                    if (variant.driverLikelihood() == DriverLikelihood.HIGH) {
                        if (isAssociatedWithDrugResistance(variant)) {
                            activatingVariantsAssociatedWithResistance.add(variant.event());
                        } else if (!isGainOfFunction) {
                            activatingVariantsWithNoGainOfFunction.add(variant.event());
                        } else if (!isPotentialOncogene) {
                            activatingVariantsInNonOncogene.add(variant.event());
                        } else {
                            activatingVariants.add(variant.event());
                        }
                    } else {
                        if (isGainOfFunction) {
                            nonHighDriverGainOfFunctionVariants.add(variant.event());
                        } else {
                            nonHighDriverVariants.add(variant.event());
                        }
                    }
                } else if (isMissenseOrHotspot(variant)) {
                    unreportableMissenseOrHotspotVariants.add(variant.event());
                }
            }
        }

        if (!activatingVariants.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addAllInclusionMolecularEvents(activatingVariants)
                    .addPassSpecificMessages(gene + " has activating mutation(s) " + Format.concat(activatingVariants))
                    .addPassGeneralMessages(gene + " activation")
                    .build();
        }

        Evaluation potentialWarnEvaluation = evaluatePotentialWarns(activatingVariantsAssociatedWithResistance,
                activatingVariantsInNonOncogene,
                activatingVariantsWithNoGainOfFunction,
                nonHighDriverGainOfFunctionVariants,
                nonHighDriverVariants,
                unreportableMissenseOrHotspotVariants);

        if (potentialWarnEvaluation != null) {
            return potentialWarnEvaluation;
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages(gene + " does not have any activating mutations")
                .addFailGeneralMessages("no " + gene + " activation")
                .build();
    }

    private static boolean isAssociatedWithDrugResistance(@NotNull Variant variant) {
        Boolean isAssociatedWithDrugResistance = variant.isAssociatedWithDrugResistance();
        return isAssociatedWithDrugResistance != null && isAssociatedWithDrugResistance;
    }

    private static boolean isMissenseOrHotspot(@NotNull Variant variant) {
        return variant.canonicalImpact().codingEffect() == CodingEffect.MISSENSE || variant.isHotspot();
    }

    @Nullable
    private Evaluation evaluatePotentialWarns(@NotNull Set<String> activatingVariantsAssociatedWithResistance,
            @NotNull Set<String> activatingVariantsInNonOncogene, @NotNull Set<String> activatingVariantsWithNoGainOfFunction,
            @NotNull Set<String> nonHighDriverGainOfFunctionVariants, @NotNull Set<String> nonHighDriverVariants,
            @NotNull Set<String> unreportableMissenseOrHotspotVariants) {
        Set<String> warnEvents = Sets.newHashSet();
        Set<String> warnSpecificMessages = Sets.newHashSet();
        Set<String> warnGeneralMessages = Sets.newHashSet();

        if (!activatingVariantsAssociatedWithResistance.isEmpty()) {
            warnEvents.addAll(activatingVariantsAssociatedWithResistance);
            warnSpecificMessages.add(gene + " has activating mutation(s) " + Format.concat(activatingVariantsAssociatedWithResistance)
                    + " but are also associated with drug resistance");
            warnGeneralMessages.add(gene + " activation");
        }

        if (!activatingVariantsInNonOncogene.isEmpty()) {
            warnEvents.addAll(activatingVariantsInNonOncogene);
            warnSpecificMessages.add(gene + " has activating mutation(s) " + Format.concat(activatingVariantsInNonOncogene)
                    + " but gene has not been not annotated as oncogene");
            warnGeneralMessages.add(gene + " activation");
        }

        if (!activatingVariantsWithNoGainOfFunction.isEmpty()) {
            warnEvents.addAll(activatingVariantsWithNoGainOfFunction);
            warnSpecificMessages.add(gene + " has activating mutation(s) " + Format.concat(activatingVariantsWithNoGainOfFunction)
                    + " but are not associated with gain-of-function");
            warnGeneralMessages.add(gene + " activation");
        }

        if (!nonHighDriverGainOfFunctionVariants.isEmpty()) {
            warnEvents.addAll(nonHighDriverGainOfFunctionVariants);
            warnSpecificMessages.add(gene + " has mutation(s) " + Format.concat(nonHighDriverGainOfFunctionVariants)
                    + " that do not have high driver likelihood but are associated with gain-of-function");
            warnGeneralMessages.add(gene + " activation");
        }

        if (!nonHighDriverVariants.isEmpty()) {
            warnEvents.addAll(nonHighDriverVariants);
            warnSpecificMessages.add(
                    gene + " has mutation(s) " + Format.concat(nonHighDriverVariants) + " that do not have a high driver likelihood");
            warnGeneralMessages.add(gene + " activation");
        }

        if (!unreportableMissenseOrHotspotVariants.isEmpty()) {
            warnEvents.addAll(unreportableMissenseOrHotspotVariants);
            warnSpecificMessages.add(gene + " has mutation(s) " + Format.concat(unreportableMissenseOrHotspotVariants)
                    + " with unreportable missense or hotspot status");
            warnGeneralMessages.add(gene + " activation");
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
