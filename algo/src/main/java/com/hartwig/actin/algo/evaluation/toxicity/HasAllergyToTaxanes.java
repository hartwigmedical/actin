package com.hartwig.actin.algo.evaluation.toxicity;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.Allergy;

import org.jetbrains.annotations.NotNull;

public class HasAllergyToTaxanes implements EvaluationFunction {

    static final Set<String> TAXANES = Sets.newHashSet();

    static {
        TAXANES.add("Paclitaxel");
        TAXANES.add("Docetaxel");
        TAXANES.add("Cabazitaxel");
    }

    HasAllergyToTaxanes() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> allergies = Sets.newHashSet();
        for (Allergy allergy : record.clinical().allergies()) {
            for (String taxane : TAXANES) {
                if (allergy.name().equalsIgnoreCase(taxane)) {
                    allergies.add(allergy.name());
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
