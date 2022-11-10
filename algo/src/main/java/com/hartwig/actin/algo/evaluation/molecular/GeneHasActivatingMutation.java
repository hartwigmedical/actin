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

public class GeneHasActivatingMutation implements EvaluationFunction {

    @NotNull
    private final String gene;

    GeneHasActivatingMutation(@NotNull final String gene) {
        this.gene = gene;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> activatingVariantsWithoutDrugResistance = Sets.newHashSet();
        Set<String> activatingVariantsAssociatedWithResistance = Sets.newHashSet();
        Set<String> activatingVariantsInNonOncogene = Sets.newHashSet();
        Set<String> nonHighDriverGainOfFunctionVariants = Sets.newHashSet();
        Set<String> nonHighDriverVariants = Sets.newHashSet();
        Set<String> highDriverNoGainOfFunctionVariants = Sets.newHashSet();
        Set<String> unreportableMissenseOrHotspotVariants = Sets.newHashSet();

        for (Variant variant : record.molecular().drivers().variants()) {
            if (variant.gene().equals(gene)) {
                boolean isActivating = isActivating(variant);

                if (variant.geneRole() == GeneRole.ONCO || variant.geneRole() == GeneRole.BOTH) {
                    if (isActivating) {
                        if (isAssociatedWithDrugResistance(variant)) {
                            activatingVariantsAssociatedWithResistance.add(variant.event());
                        } else {
                            activatingVariantsWithoutDrugResistance.add(variant.event());
                        }
                    } else {
                        if (highDriverNoGainOfFunction(variant)) {
                            highDriverNoGainOfFunctionVariants.add(variant.event());
                        }

                        if (nonHighDriver(variant)) {
                            nonHighDriverVariants.add(variant.event());

                            if (!isGainOfFunction(variant)) {
                                nonHighDriverGainOfFunctionVariants.add(variant.event());
                            }
                        }

                        if (isUnreportableMissenseOrHotspot(variant)) {
                            unreportableMissenseOrHotspotVariants.add(variant.event());
                        }
                    }
                } else if (isActivating) {
                    activatingVariantsInNonOncogene.add(variant.event());
                }
            }
        }

        if (!activatingVariantsWithoutDrugResistance.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addAllInclusionMolecularEvents(activatingVariantsWithoutDrugResistance)
                    .addPassSpecificMessages(gene + " has activating mutation(s) " + Format.concat(activatingVariantsWithoutDrugResistance))
                    .addPassGeneralMessages(gene + " activation")
                    .build();
        }

        if (!activatingVariantsAssociatedWithResistance.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addAllInclusionMolecularEvents(activatingVariantsAssociatedWithResistance)
                    .addWarnSpecificMessages(
                            gene + " has activating mutation(s) " + Format.concat(activatingVariantsAssociatedWithResistance)
                                    + " but are also associated with drug resistance")
                    .addWarnGeneralMessages(gene + " activation")
                    .build();
        }

        if (!activatingVariantsInNonOncogene.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addAllInclusionMolecularEvents(activatingVariantsInNonOncogene)
                    .addWarnSpecificMessages(gene + " has activating mutation(s) " + Format.concat(activatingVariantsInNonOncogene)
                            + " but gene has not been not annotated as oncogene")
                    .addWarnGeneralMessages(gene + " activation")
                    .build();
        }

        if (!highDriverNoGainOfFunctionVariants.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addAllInclusionMolecularEvents(highDriverNoGainOfFunctionVariants)
                    .addWarnSpecificMessages(gene + " has mutation(s) " + Format.concat(highDriverNoGainOfFunctionVariants)
                            + " that have high driver likelihood but no gain-of-function")
                    .addWarnGeneralMessages(gene + " activation")
                    .build();
        }

        if (!nonHighDriverGainOfFunctionVariants.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addAllInclusionMolecularEvents(nonHighDriverGainOfFunctionVariants)
                    .addWarnSpecificMessages(gene + " has mutation(s) " + Format.concat(nonHighDriverGainOfFunctionVariants)
                            + " that do not have high driver likelihood but are associated with gain-of-function")
                    .addWarnGeneralMessages(gene + " activation")
                    .build();
        }

        if (!nonHighDriverVariants.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addAllInclusionMolecularEvents(nonHighDriverVariants)
                    .addWarnSpecificMessages(gene + " has mutation(s) " + Format.concat(nonHighDriverVariants)
                            + " that do not have a high driver likelihood")
                    .addWarnGeneralMessages(gene + " activation")
                    .build();
        }

        if (!unreportableMissenseOrHotspotVariants.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addAllInclusionMolecularEvents(unreportableMissenseOrHotspotVariants)
                    .addWarnSpecificMessages(gene + " has mutation(s) " + Format.concat(unreportableMissenseOrHotspotVariants)
                            + " with unreportable missense or hotspot status")
                    .addWarnGeneralMessages(gene + " activation")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages(gene + " does not have any activating mutations")
                .addFailGeneralMessages("no " + gene + " activation")
                .build();
    }

    private static boolean isActivating(@NotNull Variant variant) {
        boolean isHighDriver = variant.driverLikelihood() == DriverLikelihood.HIGH;
        boolean isGainOfFunction = isGainOfFunction(variant);
        return variant.isReportable() && isHighDriver && isGainOfFunction;
    }

    private static boolean isAssociatedWithDrugResistance(@NotNull Variant variant) {
        Boolean isAssociatedWithDrugResistance = variant.isAssociatedWithDrugResistance();
        return isAssociatedWithDrugResistance != null && isAssociatedWithDrugResistance;
    }

    private static boolean highDriverNoGainOfFunction(@NotNull Variant variant) {
        boolean isHighDriver = variant.driverLikelihood() == DriverLikelihood.HIGH;
        boolean isGainOfFunction = isGainOfFunction(variant);
        return variant.isReportable() && isHighDriver && !isGainOfFunction;
    }

    private static boolean nonHighDriver(@NotNull Variant variant) {
        boolean isNonHighDriver = variant.driverLikelihood() != DriverLikelihood.HIGH;
        return variant.isReportable() && isNonHighDriver;
    }

    private static boolean isUnreportableMissenseOrHotspot(@NotNull Variant variant) {
        boolean isMissenseOrHotspot = variant.canonicalImpact().codingEffect() == CodingEffect.MISSENSE || variant.isHotspot();
        return !variant.isReportable() && isMissenseOrHotspot;
    }

    private static boolean isGainOfFunction(@NotNull Variant variant) {
        return variant.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION
                || variant.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION_PREDICTED;
    }
}
