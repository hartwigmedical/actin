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

    private static final double CLONAL_CUTOFF = 0.5;
    private static final double CLONAL_CUTOFF_PERCENTAGE = CLONAL_CUTOFF*100;

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
        Set<String> otherMissenseOrHotspotVariants = Sets.newHashSet();
        Set<String> activatingSubclonalVariants = Sets.newHashSet();

        Boolean hasHighMutationalLoad = record.molecular().characteristics().hasHighTumorMutationalLoad();
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
                        } else if (variant.clonalLikelihood() < CLONAL_CUTOFF) {
                            activatingSubclonalVariants.add(variant.event());
                        } else {
                            activatingVariants.add(variant.event());
                        }
                    } else {
                        if (isGainOfFunction) {
                            nonHighDriverGainOfFunctionVariants.add(variant.event());
                        } else if (hasHighMutationalLoad == null || !hasHighMutationalLoad) {
                            nonHighDriverVariants.add(variant.event());
                        }
                    }
                } else if (isMissenseOrHotspot(variant)) {
                    otherMissenseOrHotspotVariants.add(variant.event());
                }
            }
        }

        if (!activatingVariants.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addAllInclusionMolecularEvents(activatingVariants)
                    .addPassSpecificMessages("Activating mutation(s) detected in gene + " + gene + ": " + Format.concat(activatingVariants))
                    .addPassGeneralMessages(gene + " activating mutation(s)")
                    .build();
        }

        Evaluation potentialWarnEvaluation = evaluatePotentialWarns(activatingVariantsAssociatedWithResistance,
                activatingVariantsInNonOncogene,
                activatingVariantsWithNoGainOfFunction,
                nonHighDriverGainOfFunctionVariants,
                nonHighDriverVariants,
                otherMissenseOrHotspotVariants,
                activatingSubclonalVariants);

        if (potentialWarnEvaluation != null) {
            return potentialWarnEvaluation;
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No activating mutation(s) detected in gene " + gene)
                .addFailGeneralMessages("No " + gene + " activating mutation(s)")
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
            @NotNull Set<String> otherMissenseOrHotspotVariants, @NotNull Set<String> highSubclonalLikelihoodReportableVariants) {
        Set<String> warnEvents = Sets.newHashSet();
        Set<String> warnSpecificMessages = Sets.newHashSet();
        Set<String> warnGeneralMessages = Sets.newHashSet();

        if (!activatingVariantsAssociatedWithResistance.isEmpty()) {
            warnEvents.addAll(activatingVariantsAssociatedWithResistance);
            warnSpecificMessages.add(
                    "Gene " + gene + " should have activating mutation(s): " + Format.concat(activatingVariantsAssociatedWithResistance)
                            + ", however, these are (also) associated with drug resistance");
            warnGeneralMessages.add(gene + " activating mutation(s) detected but associated with drug resistance");
        }

        if (!activatingVariantsInNonOncogene.isEmpty()) {
            warnEvents.addAll(activatingVariantsInNonOncogene);
            warnSpecificMessages.add("Gene " + gene + " has activating mutation(s) " + Format.concat(activatingVariantsInNonOncogene)
                    + " but gene has not been not annotated as oncogene");
            warnGeneralMessages.add(gene + " activating mutation(s) detected but " + gene + " unknown as oncogene");
        }

        if (!activatingVariantsWithNoGainOfFunction.isEmpty()) {
            warnEvents.addAll(activatingVariantsWithNoGainOfFunction);
            warnSpecificMessages.add(
                    "Gene " + gene + " has potentially activating mutation(s) " + Format.concat(activatingVariantsWithNoGainOfFunction)
                            + " that have high driver likelihood, but are not associated with protein effect gain-of-function");
            warnGeneralMessages.add(
                    gene + " potentially activating mutation(s) detected but not associated with having gain-of-function protein effect");
        }

        if (!nonHighDriverGainOfFunctionVariants.isEmpty()) {
            warnEvents.addAll(nonHighDriverGainOfFunctionVariants);
            warnSpecificMessages.add(
                    "Gene " + gene + " has potentially activating mutation(s) " + Format.concat(nonHighDriverGainOfFunctionVariants)
                            + " that do not have high driver likelihood, but are associated with gain-of-function protein effect");
            warnGeneralMessages.add(
                    gene + " potentially activating mutation(s) detected based on protein effect but no high driver likelihood");
        }

        if (!nonHighDriverVariants.isEmpty()) {
            warnEvents.addAll(nonHighDriverVariants);
            warnSpecificMessages.add("Gene " + gene + " has potentially activating mutation(s) " + Format.concat(nonHighDriverVariants)
                    + " but no high driver likelihood");
            warnGeneralMessages.add(gene + " potentially activating mutation(s) detected but no high driver likelihood");
        }

        if (!otherMissenseOrHotspotVariants.isEmpty()) {
            warnEvents.addAll(otherMissenseOrHotspotVariants);
            warnSpecificMessages.add(
                    "Gene " + gene + " has potentially activating mutation(s) " + Format.concat(otherMissenseOrHotspotVariants)
                            + " that are missense or have hotspot status, but are not considered reportable");
            warnGeneralMessages.add(gene + " potentially activating mutation(s) detected but is unreportable");
        }

        if (!highSubclonalLikelihoodReportableVariants.isEmpty()) {
            warnEvents.addAll(highSubclonalLikelihoodReportableVariants);
            warnSpecificMessages.add(
                    "Gene " + gene + " potentially activating mutation(s) " + Format.concat(highSubclonalLikelihoodReportableVariants)
                            + " have subclonal likelihood of > " + CLONAL_CUTOFF_PERCENTAGE + "%");
            warnGeneralMessages.add(gene + " potentially activating mutation(s) " + Format.concat(highSubclonalLikelihoodReportableVariants)
                    + " but subclonal likelihood > " + CLONAL_CUTOFF_PERCENTAGE + "%");
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
