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

//TODO: Check implementation
public class GeneHasVariantInCodon implements EvaluationFunction {

    @NotNull
    private final String gene;
    @NotNull
    private final List<String> codons;

    GeneHasVariantInCodon(@NotNull final String gene, @NotNull final List<String> codons) {
        this.gene = gene;
        this.codons = codons;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> codonsCanonicalFound = Sets.newHashSet();
        Set<String> codonsOtherFound = Sets.newHashSet();

        for (Variant variant : record.molecular().drivers().variants()) {
            if (variant.gene().equals(gene)) {

                // boolean isReportable = false;
                // if (variant.isReportable() == true) {
                //     isReportable = true;
                // }

                for (String codon : codons) {
                    boolean codonOtherMatch = false;
                    for (TranscriptImpact otherImpact : variant.otherImpacts()) {
                        if (otherImpact.affectedCodon() != null && otherImpact.affectedCodon()
                                .toString()
                                .equals(codon.replaceAll("[^0-9]", ""))) {
                            codonOtherMatch = true;
                        }
                    }

                    if (variant.canonicalImpact().affectedCodon() != null && variant.canonicalImpact()
                            .affectedCodon()
                            .toString()
                            .equals(codon.replaceAll("[^0-9]", ""))) {
                        codonsCanonicalFound.add(codon);
                    }

                    if (codonOtherMatch) {
                        codonsOtherFound.add(codon);
                    }
                }

            }
        }
        if (!codonsCanonicalFound.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages(
                            "Variant(s) in codon(s) " + Format.concat(codonsCanonicalFound) + " in gene " + gene + " detected in canonical transcript")
                    .addPassGeneralMessages("Variant(s) in codon(s) " + Format.concat(codonsCanonicalFound) + " found in " + gene)
                    .build();
        } else if (!codonsOtherFound.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addWarnSpecificMessages("Variant(s) in codon(s) " + Format.concat(codonsOtherFound) + " in " + gene
                            + " detected, but in non-canonical transcript")
                    .addWarnGeneralMessages("Variant(s) in codon(s) " + Format.concat(codonsCanonicalFound) + " found in non-canonical transcript of gene " + gene)
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No variants in codon(s) " + Format.concat(codons) + " detected in gene " + gene)
                .addFailGeneralMessages("No specific variants in codon(s) in " + gene + " detected")
                .build();
    }
}