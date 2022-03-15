package com.hartwig.actin.algo.evaluation.washout;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;

public class HasRecentlyReceivedRadiotherapy implements EvaluationFunction {

    private final int referenceYear;
    private final int referenceMonth;

    HasRecentlyReceivedRadiotherapy(final int referenceYear, final int referenceMonth) {
        this.referenceYear = referenceYear;
        this.referenceMonth = referenceMonth;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasReceivedRadiotherapy = false;
        for (PriorTumorTreatment priorTumorTreatment : record.clinical().priorTumorTreatments()) {
            if (priorTumorTreatment.categories().contains(TreatmentCategory.RADIOTHERAPY)) {
                Integer year = priorTumorTreatment.year();
                Integer month = priorTumorTreatment.month();

                if ((year == null) || (year == referenceYear && (month == null || month == referenceMonth))) {
                    hasReceivedRadiotherapy = true;
                }
            }
        }

        EvaluationResult result = hasReceivedRadiotherapy ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has not recently received radiotherapy");
        } else if (result.isPass()) {
            builder.addPassSpecificMessages("Patient has recently received radiotherapy");
        }

        return builder.build();
    }
}
