package com.hartwig.actin.algo.evaluation.molecular;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.GeneRole;
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.util.MolecularEventFactory;

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
        Set<String> activatingVariants = Sets.newHashSet();
        Set<String> activatingVariantsAssociatedWithResistance = Sets.newHashSet();
        Set<String> nonHighDriverGainOfFunctionVariants = Sets.newHashSet();
        Set<String> nonHighDriverVariants = Sets.newHashSet();
        Set<String> highDriverNoGainOfFunctionVariants = Sets.newHashSet();
        Set<String> variantWithUnclearHotspotStatus = Sets.newHashSet();
        Set<String> unreportableMissenseVariants = Sets.newHashSet();
        Set<String> variantsAssociatedWithResistance = Sets.newHashSet();

        for (Variant variant : record.molecular().drivers().variants()) {
            if (variant.gene().equals(gene)) {
                String variantEvent = MolecularEventFactory.variantEvent(variant);
                if (variant.geneRole() == GeneRole.ONCO || variant.geneRole() == GeneRole.BOTH) {
                    if (isActivating(variant)) {
                        if (isAssociatedWithDrugResistance(variant)) {
                            activatingVariantsAssociatedWithResistance.add(variantEvent);
                        } else {
                            activatingVariants.add(variantEvent);
                        }
                    } else if (variant.geneRole() == GeneRole.ONCO) {

                    }
                }
            }
        }

        return EvaluationFactory.unrecoverable().result(EvaluationResult.FAIL)
                .addFailSpecificMessages("fail")
                .addFailGeneralMessages("fail")
                .build();
    }

    private static boolean isActivating(@NotNull Variant variant) {
        boolean isHighDriver = variant.driverLikelihood() == DriverLikelihood.HIGH;
        boolean hasSuitableGeneRole = variant.geneRole() == GeneRole.ONCO || variant.geneRole() == GeneRole.BOTH;
        boolean hasSuitableProteinEffect = variant.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION
                || variant.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION_PREDICTED;
        return variant.isReportable() && isHighDriver && hasSuitableGeneRole && hasSuitableProteinEffect;
    }

    private static boolean isAssociatedWithDrugResistance(@NotNull Variant variant) {
        Boolean isAssociatedWithDrugResistance = variant.isAssociatedWithDrugResistance();
        return isAssociatedWithDrugResistance != null && isAssociatedWithDrugResistance;
    }
}
