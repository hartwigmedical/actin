package com.hartwig.actin.algo.evaluation.toxicity;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.Intolerance;

import org.jetbrains.annotations.NotNull;

public class HasIntoleranceToTaxanes implements EvaluationFunction {

    static final Set<String> TAXANES = Sets.newHashSet();

    static {
        TAXANES.add("Paclitaxel");
        TAXANES.add("Docetaxel");
        TAXANES.add("Cabazitaxel");
    }

    HasIntoleranceToTaxanes() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> allergies = Sets.newHashSet();
        for (Intolerance intolerance : record.clinical().intolerances()) {
            for (String taxane : TAXANES) {
                if (intolerance.name().equalsIgnoreCase(taxane)) {
                    allergies.add(intolerance.name());
                }
            }
        }

        if (!allergies.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has allergy to a taxane: " + Format.concat(allergies))
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient has no known allergy to taxanes")
                .build();
    }
}
