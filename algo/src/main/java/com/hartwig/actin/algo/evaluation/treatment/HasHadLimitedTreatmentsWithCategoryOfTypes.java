package com.hartwig.actin.algo.evaluation.treatment;

import java.util.List;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;

public class HasHadLimitedTreatmentsWithCategoryOfTypes implements EvaluationFunction {

    @NotNull
    private final TreatmentCategory category;
    @NotNull
    private final List<String> types;
    private final int maxTreatmentLines;

    HasHadLimitedTreatmentsWithCategoryOfTypes(@NotNull final TreatmentCategory category, @NotNull final List<String> types,
            final int maxTreatmentLines) {
        this.category = category;
        this.types = types;
        this.maxTreatmentLines = maxTreatmentLines;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        int numTreatmentLines = 0;
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (treatment.categories().contains(category)) {
                boolean matchWithAnyType = false;
                for (String type : types) {
                    if (TreatmentTypeResolver.isOfType(treatment, category, type)) {
                        matchWithAnyType = true;
                    }
                }
                if (matchWithAnyType) {
                    numTreatmentLines++;
                }
            }
        }

        EvaluationResult result = numTreatmentLines <= maxTreatmentLines ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has not received at most " + maxTreatmentLines + " lines of " + Format.concat(types) + " "
                    + category.display());
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages(
                    "Patient has received at most " + maxTreatmentLines + " lines of " + Format.concat(types) + " " + category.display());
        }

        return builder.build();
    }
}
