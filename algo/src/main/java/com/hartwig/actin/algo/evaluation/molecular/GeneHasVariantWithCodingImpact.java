package com.hartwig.actin.algo.evaluation.molecular;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.molecular.datamodel.driver.TranscriptImpact;
import com.hartwig.actin.molecular.datamodel.driver.Variant;

import org.jetbrains.annotations.NotNull;

public class GeneHasVariantWithCodingImpact implements EvaluationFunction {

    @NotNull
    private final String gene;
    @NotNull
    private final List<String> allowedCodingImpacts;

    GeneHasVariantWithCodingImpact(@NotNull final String gene, @NotNull final List<String> allowedCodingImpacts) {
        this.gene = gene;
        this.allowedCodingImpacts = allowedCodingImpacts;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> codingImpactsCanonicalFound = Sets.newHashSet();
        Set<String> codingImpactsOtherFound = Sets.newHashSet();

        for (Variant variant : record.molecular().drivers().variants()) {
            if (variant.gene().equals(gene)) {
                for (String allowedCodingImpact : allowedCodingImpacts) {
                    boolean codingImpactOtherMatch = false;
                    for (TranscriptImpact otherImpact : variant.otherImpacts()) {
                        if (otherImpact.codingImpact().equals(allowedCodingImpact)) {
                            codingImpactOtherMatch = true;
                        }
                    }

                    if (variant.canonicalImpact().codingImpact().equals(allowedCodingImpact)) {
                        codingImpactsCanonicalFound.add(allowedCodingImpact);
                    }

                    if (codingImpactOtherMatch) {
                        codingImpactsOtherFound.add(allowedCodingImpact);
                    }
                }
            }
        }

        if (!codingImpactsCanonicalFound.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Variant(s) " + Format.concat(codingImpactsCanonicalFound) + " in gene " + gene
                            + " detected in canonical transcript")
                    .addPassGeneralMessages(Format.concat(codingImpactsCanonicalFound) + " found in " + gene)
                    .build();
        } else if (!codingImpactsOtherFound.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addWarnSpecificMessages("Variant(s) " + Format.concat(codingImpactsOtherFound) + " in " + gene
                            + " detected, but in non-canonical transcript")
                    .addWarnGeneralMessages(Format.concat(codingImpactsOtherFound) + " found in non-canonical transcript of gene " + gene)
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("None of " + Format.concat(allowedCodingImpacts) + " detected in gene " + gene)
                .addFailGeneralMessages("No specific variants in " + gene + " detected")
                .build();
    }
}