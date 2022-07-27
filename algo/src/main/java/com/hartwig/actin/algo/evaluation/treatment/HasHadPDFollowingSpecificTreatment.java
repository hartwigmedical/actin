package com.hartwig.actin.algo.evaluation.treatment;

import java.util.Set;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HasHadPDFollowingSpecificTreatment implements EvaluationFunction {

    static final String STOP_REASON_PD = "PD";

    @NotNull
    private final Set<String> names;
    @Nullable
    private final TreatmentCategory warnCategory;

    HasHadPDFollowingSpecificTreatment(@NotNull final Set<String> names, @Nullable final TreatmentCategory warnCategory) {
        this.names = names;
        this.warnCategory = warnCategory;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasHadPDFollowingTreatment = false;
        boolean hasHadTreatmentWithUnclearStopReason = false;
        boolean hasHadTreatmentWithExactType = false;
        boolean hasHadTreatmentWithWarnType = false;

        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (warnCategory != null && treatment.categories().contains(warnCategory)) {
                hasHadTreatmentWithWarnType = true;
            }

            String stopReason = treatment.stopReason();
            for (String name : names) {
                if (treatment.name().toLowerCase().contains(name.toLowerCase())) {
                    hasHadTreatmentWithExactType = true;
                    if (stopReason != null) {
                        if (stopReason.equalsIgnoreCase(STOP_REASON_PD)) {
                            hasHadPDFollowingTreatment = true;
                        }
                    } else {
                        hasHadTreatmentWithUnclearStopReason = true;
                    }
                }
            }
        }

        EvaluationResult result;
        if (hasHadPDFollowingTreatment) {
            result = EvaluationResult.PASS;
        } else if (hasHadTreatmentWithUnclearStopReason || hasHadTreatmentWithWarnType) {
            result = EvaluationResult.UNDETERMINED;
        } else {
            result = EvaluationResult.FAIL;
        }

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            if (hasHadTreatmentWithExactType) {
                builder.addFailSpecificMessages("Patient has received specific treatment but without stop reason PD");
            } else {
                builder.addFailSpecificMessages("No treatment");
            }
            builder.addFailGeneralMessages("Systemic treatments");
        } else if (result == EvaluationResult.UNDETERMINED) {
            if (hasHadTreatmentWithWarnType) {
                builder.addUndeterminedSpecificMessages("Can't determine whether patient has received specific treatment ");
            } else {
                builder.addUndeterminedSpecificMessages("Patient has received specific treatment but with unclear stop reason");
            }
            builder.addUndeterminedGeneralMessages("Unclear treatment");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient has received specific treatment with stop reason PD");
            builder.addPassGeneralMessages("Treatment");
        }

        return builder.build();
    }
}
