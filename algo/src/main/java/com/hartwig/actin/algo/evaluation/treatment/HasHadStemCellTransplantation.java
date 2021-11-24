package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;

public class HasHadStemCellTransplantation implements EvaluationFunction {

    HasHadStemCellTransplantation() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull final PatientRecord record) {
        for (PriorTumorTreatment priorTumorTreatment : record.clinical().priorTumorTreatments()) {
            if (priorTumorTreatment.categories().contains(TreatmentCategory.STEM_CELL_TRANSPLANTATION)) {
                return Evaluation.PASS;
            }
        }

        return Evaluation.FAIL;
    }
}
