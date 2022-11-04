package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.molecular.datamodel.driver.CodingContext;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.RegionType;
import com.hartwig.actin.molecular.datamodel.driver.TranscriptImpact;
import com.hartwig.actin.molecular.datamodel.driver.Variant;

import org.jetbrains.annotations.NotNull;

public class GeneHasUTR3Loss implements EvaluationFunction {

    static final String THREE_UTR_EFFECT = "3_prime_UTR_variant";

    @NotNull
    private final String gene;

    GeneHasUTR3Loss(@NotNull final String gene) {
        this.gene = gene;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean has3UTRHotspot = false;
        boolean has3UTRVUS = false;
        for (Variant variant : record.molecular().drivers().variants()) {
            if (variant.gene().equals(gene) && has3UTRCanonicalEffect(variant)) {
                if (variant.isHotspot()) {
                    has3UTRHotspot = true;
                } else {
                    has3UTRVUS = true;
                }
            }
        }

        boolean has3UTRDisruption = false;
        for (Disruption disruption : record.molecular().drivers().disruptions()) {
            if (disruption.gene().equals(gene) && disruption.codingContext() == CodingContext.UTR_3P
                    && disruption.regionType() == RegionType.EXONIC) {
                has3UTRDisruption = true;
            }
        }

        if (has3UTRHotspot) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("3' UTR hotspot mutation in " + gene + " should lead to 3' UTR loss")
                    .addPassGeneralMessages("3' UTR loss of " + gene)
                    .build();
        } else if (has3UTRDisruption) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addWarnSpecificMessages("Disruption detected in 3' UTR region of " + gene + " which may lead to 3' UTR loss")
                    .addWarnGeneralMessages("Potential 3' UTR loss of " + gene)
                    .build();
        } else if (has3UTRVUS) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addWarnSpecificMessages("VUS mutation detected in 3' UTR region of " + gene + " which may lead to 3' UTR loss")
                    .addWarnGeneralMessages("Potential 3' UTR loss of " + gene)
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No variants detected in 3' UTR region of " + gene)
                .addFailGeneralMessages("No 3' UTR loss of " + gene)
                .build();
    }

    private static boolean has3UTRCanonicalEffect(@NotNull Variant variant) {
        for (TranscriptImpact impact : variant.impacts()) {
            if (impact.isCanonical() && impact.effect().equals(THREE_UTR_EFFECT)) {
                return true;
            }
        }
        return false;
    }
}
