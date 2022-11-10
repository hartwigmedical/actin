package com.hartwig.actin.algo.evaluation.molecular;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect;
import com.hartwig.actin.molecular.util.MolecularEventFactory;

import org.jetbrains.annotations.NotNull;

public class HasFusionInGene implements EvaluationFunction {

    @NotNull
    private final String gene;

    HasFusionInGene(@NotNull final String gene) {
        this.gene = gene;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> matchingFusions = Sets.newHashSet();
        Set<String> fusionsWithNoEffect = Sets.newHashSet();
        Set<String> fusionsWithNoHighDriverLikelihood = Sets.newHashSet();
        Set<String> unreportableFusionsWithGainOfFunction = Sets.newHashSet();

        for (Fusion fusion : record.molecular().drivers().fusions()) {
            if (fusion.geneStart().equals(gene) || fusion.geneEnd().equals(gene)) {
                String fusionEvent = MolecularEventFactory.fusionEvent(fusion);
                if (fusion.isReportable()) {
                    boolean hasNoEffect = fusion.proteinEffect() == ProteinEffect.NO_EFFECT
                            || fusion.proteinEffect() == ProteinEffect.NO_EFFECT_PREDICTED;
                    if (fusion.driverLikelihood() != DriverLikelihood.HIGH) {
                        fusionsWithNoHighDriverLikelihood.add(fusionEvent);
                    } else if (hasNoEffect) {
                        fusionsWithNoEffect.add(fusionEvent);
                    } else {
                        matchingFusions.add(fusionEvent);
                    }
                } else {
                    boolean isGainOfFunction = fusion.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION
                            || fusion.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION_PREDICTED;
                    if (isGainOfFunction) {
                        unreportableFusionsWithGainOfFunction.add(fusionEvent);
                    }
                }
            }
        }

        if (!matchingFusions.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addAllInclusionMolecularEvents(matchingFusions)
                    .addPassSpecificMessages("Fusion(s) " + Format.concat(matchingFusions) + " detected in gene " + gene)
                    .addPassGeneralMessages("Fusion(s) detected in gene " + gene)
                    .build();
        }

        if (!fusionsWithNoEffect.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addAllInclusionMolecularEvents(fusionsWithNoEffect)
                    .addWarnSpecificMessages("Fusion(s) " + Format.concat(fusionsWithNoEffect) + " detected in gene " + gene
                            + " but annotated as having no effect")
                    .addWarnGeneralMessages("Potential fusion(s) detected in gene " + gene)
                    .build();
        }

        if (!fusionsWithNoHighDriverLikelihood.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addAllInclusionMolecularEvents(fusionsWithNoHighDriverLikelihood)
                    .addWarnSpecificMessages("Fusion(s) " + Format.concat(fusionsWithNoHighDriverLikelihood) + " detected in gene " + gene
                            + " but annotated as having no high driver likelihood")
                    .addWarnGeneralMessages("Potential fusion(s) detected in gene " + gene)
                    .build();
        }

        if (!unreportableFusionsWithGainOfFunction.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addAllInclusionMolecularEvents(unreportableFusionsWithGainOfFunction)
                    .addWarnSpecificMessages(
                            "Fusion(s) " + Format.concat(unreportableFusionsWithGainOfFunction) + " detected in gene " + gene
                                    + " but not considered reportable yet having gain-of-function")
                    .addWarnGeneralMessages("Potential fusion(s) detected in gene " + gene)
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No fusion detected with gene " + gene)
                .addFailGeneralMessages("Molecular requirements")
                .build();
    }
}
