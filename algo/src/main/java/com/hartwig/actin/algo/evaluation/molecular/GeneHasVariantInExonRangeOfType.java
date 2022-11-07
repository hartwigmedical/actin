package com.hartwig.actin.algo.evaluation.molecular;

import javax.annotation.Nullable;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.molecular.datamodel.driver.TranscriptImpact;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.driver.VariantType;
import com.hartwig.actin.treatment.input.datamodel.VariantTypeInput;

import org.jetbrains.annotations.NotNull;

public class GeneHasVariantInExonRangeOfType implements EvaluationFunction {

    @NotNull
    private final String gene;
    private final int minExon;
    private final int maxExon;
    @Nullable
    private final VariantTypeInput requiredVariantType;

    GeneHasVariantInExonRangeOfType(@NotNull final String gene, final int minExon, final int maxExon,
            @Nullable final VariantTypeInput requiredVariantType) {
        this.gene = gene;
        this.minExon = minExon;
        this.maxExon = maxExon;
        this.requiredVariantType = requiredVariantType;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        for (Variant variant : record.molecular().drivers().variants()) {
            if (variant.gene().equals(gene)) {

                // boolean isReportable = false;
                // if (variant.isReportable() == true) {
                //     isReportable = true;
                // }

                boolean exonRangeCanonicalMatch = false;
                if (variant.canonicalImpact().affectedExon() != null) {
                    if (variant.canonicalImpact().affectedExon() >= minExon && variant.canonicalImpact().affectedExon() <= maxExon) {
                        exonRangeCanonicalMatch = true;
                    }
                }

                boolean exonRangeOtherMatch = false;
                for (TranscriptImpact otherImpact : variant.otherImpacts()) {
                    if (otherImpact.affectedExon() != null && otherImpact.affectedExon() >= minExon
                            && otherImpact.affectedExon() <= maxExon) {
                        exonRangeOtherMatch = true;
                    }
                }

                //TODO: Clean fix
                boolean requiredVariantTypeMatch = false;
                if (requiredVariantType == null) {
                    requiredVariantTypeMatch = true;
                } else if ((requiredVariantType.equals(VariantTypeInput.INSERT) && variant.type().equals(VariantType.INSERT)) || (
                        requiredVariantType.equals(VariantTypeInput.DELETE) && variant.type().equals(VariantType.DELETE)) || (
                        requiredVariantType.equals(VariantTypeInput.MNV) && variant.type().equals(VariantType.MNV)) || (
                        requiredVariantType.equals(VariantTypeInput.SNV) && variant.type().equals(VariantType.SNV))) {
                    requiredVariantTypeMatch = true;
                } else if (requiredVariantType.equals(VariantTypeInput.INDEL) && (variant.type().equals(VariantType.INSERT)
                        || variant.type().equals(VariantType.DELETE))) {
                    requiredVariantTypeMatch = true;
                }

                if (exonRangeCanonicalMatch && requiredVariantTypeMatch) {
                    return EvaluationFactory.unrecoverable()
                            .result(EvaluationResult.PASS)
                            .addPassSpecificMessages("Variant(s) in exon range " + minExon + " - " + maxExon + " in gene " + gene
                                    + " of adequate type detected in canonical transcript")
                            .addPassGeneralMessages("Adequate variant(s) found in " + gene)
                            .build();
                    // } else if (exonRangeCanonicalMatch && requiredVariantTypeMatch) {
                    //     return EvaluationFactory.unrecoverable()
                    //             .result(EvaluationResult.WARN)
                    //             .addWarnSpecificMessages("Variant(s) in exon range " + minExon + " - " + maxExon + " in gene " + gene
                    //                     + " of adequate type detected in canonical transcript, but considered non-reportable")
                    //             .addWarnGeneralMessages("Adequate variant(s) found in " + gene)
                    //             .build();

                } else if (exonRangeOtherMatch && requiredVariantTypeMatch) {
                    return EvaluationFactory.unrecoverable()
                            .result(EvaluationResult.WARN)
                            .addWarnSpecificMessages("Variant(s) in exon range " + minExon + " - " + maxExon + " in gene " + gene
                                    + " of adequate type detected, but in non-canonical transcript")
                            .addWarnGeneralMessages("Adequate variant(s) found in non-canonical transcript of gene " + gene)
                            .build();
                    // } else if (exonRangeOtherMatch && requiredVariantTypeMatch) {
                    //     return EvaluationFactory.unrecoverable()
                    //             .result(EvaluationResult.WARN)
                    //             .addWarnSpecificMessages("Variant(s) in exon range " + minExon + " - " + maxExon + " in gene " + gene
                    //                     + " of adequate type detected in canonical transcript, but considered non-reportable")
                    //             .addWarnGeneralMessages("Adequate variant(s) found in " + gene)
                    //             .build();
                }
            }
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No adequate variant in exon range " + minExon + " - " + maxExon + " detected in gene " + gene)
                .addFailGeneralMessages("No specific variants in " + gene + " detected")
                .build();
    }
}
