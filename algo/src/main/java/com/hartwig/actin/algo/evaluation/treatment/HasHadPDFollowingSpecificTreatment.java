package com.hartwig.actin.algo.evaluation.treatment;

import java.util.Set;

import com.google.common.collect.Sets;
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
        Set<String> treatmentsWithPD = Sets.newHashSet();
        Set<String> treatmentsWithExactType = Sets.newHashSet();
        boolean hasHadTreatmentWithUnclearStopReason = false;
        boolean hasHadTreatmentWithWarnType = false;

        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (warnCategory != null && treatment.categories().contains(warnCategory)) {
                hasHadTreatmentWithWarnType = true;
            }

            String stopReason = treatment.stopReason();
            for (String name : names) {
                if (treatment.name().toLowerCase().contains(name.toLowerCase())) {
                    treatmentsWithExactType.add(treatment.name());
                    if (stopReason != null) {
                        if (stopReason.equalsIgnoreCase(STOP_REASON_PD)) {
                            treatmentsWithPD.add(treatment.name());
                        }
                    } else {
                        hasHadTreatmentWithUnclearStopReason = true;
                    }
                }
            }
        }

        EvaluationResult result;
        if (!treatmentsWithPD.isEmpty()) {
            result = EvaluationResult.PASS;
        } else if (hasHadTreatmentWithUnclearStopReason || hasHadTreatmentWithWarnType) {
            result = EvaluationResult.UNDETERMINED;
        } else {
            result = EvaluationResult.FAIL;
        }

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            if (!treatmentsWithExactType.isEmpty()) {
                builder.addFailSpecificMessages(
                        "Patient has received " + Format.concat(treatmentsWithExactType) + " treatment, but stop reason was not PD");
            } else {
                builder.addFailSpecificMessages("Patient has not received specific " + Format.concat(names) + " treatment");
            }
            builder.addFailGeneralMessages("No treatment with PD");
        } else if (result == EvaluationResult.UNDETERMINED) {
            if (hasHadTreatmentWithWarnType) {
                builder.addUndeterminedSpecificMessages(
                        "Undetermined whether patient has received specific " + Format.concat(names) + " treatment");
            } else {
                builder.addUndeterminedSpecificMessages("Patient has received " + Format.concat(treatmentsWithExactType)
                        + " treatment but undetermined if stop reason is PD");
            }
            builder.addUndeterminedGeneralMessages("Undetermined treatment with PD");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages(
                    "Patient has received specific " + Format.concat(treatmentsWithPD) + " treatment with stop reason PD");
            builder.addPassGeneralMessages("Treatment with PD");
        }

        return builder.build();
    }
}
