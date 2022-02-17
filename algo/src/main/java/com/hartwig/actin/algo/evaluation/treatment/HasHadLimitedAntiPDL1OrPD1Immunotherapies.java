package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;

public class HasHadLimitedAntiPDL1OrPD1Immunotherapies implements EvaluationFunction {

    static final String PD1_TYPE = "ANTI-PD-1";
    static final String PDL1_TYPE = "ANTI-PD-L1";

    private final int maxAntiPDL1OrPD1Immunotherapies;

    HasHadLimitedAntiPDL1OrPD1Immunotherapies(final int maxAntiPDL1OrPD1Immunotherapies) {
        this.maxAntiPDL1OrPD1Immunotherapies = maxAntiPDL1OrPD1Immunotherapies;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        // TODO Remove
        int count = 0;
        for (PriorTumorTreatment priorTumorTreatment : record.clinical().priorTumorTreatments()) {
            boolean isImmunotherapy = priorTumorTreatment.categories().contains(TreatmentCategory.IMMUNOTHERAPY);
            boolean isAntiPD1Type = PD1_TYPE.equals(priorTumorTreatment.immunoType());
            boolean isAntiPDL1Type = PDL1_TYPE.equals(priorTumorTreatment.immunoType());

            if (isImmunotherapy && (isAntiPD1Type || isAntiPDL1Type)) {
                count++;
            }
        }

        EvaluationResult result = count <= maxAntiPDL1OrPD1Immunotherapies ? EvaluationResult.PASS : EvaluationResult.FAIL;
        return EvaluationFactory.create(result);
    }
}
