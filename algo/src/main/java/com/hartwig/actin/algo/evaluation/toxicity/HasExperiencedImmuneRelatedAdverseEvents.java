package com.hartwig.actin.algo.evaluation.toxicity;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;

public class HasExperiencedImmuneRelatedAdverseEvents implements EvaluationFunction {

    HasExperiencedImmuneRelatedAdverseEvents() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasHadImmuneTherapy = false;
        for (PriorTumorTreatment priorTumorTreatment : record.clinical().priorTumorTreatments()) {
            if (priorTumorTreatment.categories().contains(TreatmentCategory.IMMUNOTHERAPY)) {
                hasHadImmuneTherapy = true;
            }
        }

        EvaluationResult result = hasHadImmuneTherapy ? EvaluationResult.WARN : EvaluationResult.FAIL;

        //TODO: Update according to README
        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has not experienced immune related adverse events");
        } else if (result == EvaluationResult.WARN) {
            builder.addWarnSpecificMessages("Patient may have experienced immune related adverse events by immunotherapy treatment");
            builder.addWarnGeneralMessages("Potential immunotherapy related adverse events");
        }
        return builder.build();
    }
}
