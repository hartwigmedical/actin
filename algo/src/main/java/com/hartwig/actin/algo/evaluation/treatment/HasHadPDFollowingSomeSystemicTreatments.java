package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasHadPDFollowingSomeSystemicTreatments implements EvaluationFunction {

    static final String STOP_REASON_PD = "PD";

    private final int minSystemicTreatments;

    HasHadPDFollowingSomeSystemicTreatments(final int minSystemicTreatments) {
        this.minSystemicTreatments = minSystemicTreatments;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        int minSystemicCount = SystemicTreatmentAnalyser.minSystemicTreatments(record.clinical().priorTumorTreatments());
        int maxSystemicCount = SystemicTreatmentAnalyser.maxSystemicTreatments(record.clinical().priorTumorTreatments());

        if (minSystemicCount >= minSystemicTreatments) {
            String stopReason = SystemicTreatmentAnalyser.stopReasonOnLastSystemicTreatment(record.clinical().priorTumorTreatments());
            if (stopReason != null && stopReason.equals(STOP_REASON_PD)) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.PASS)
                        .addPassSpecificMessages(
                                "Patient received at least " + minSystemicTreatments + " systemic treatments with final stop reason PD")
                        .addPassGeneralMessages("Nr of systemic treatments")
                        .build();
            } else {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.UNDETERMINED)
                        .addUndeterminedSpecificMessages("Patient received at least " + minSystemicTreatments
                                + " systemic treatments but undetermined final stop reason")
                        .addUndeterminedGeneralMessages("Nr of systemic treatments with PD")
                        .build();
            }
        } else if (maxSystemicCount >= minSystemicTreatments) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages(
                            "Undetermined if patient received at least " + minSystemicTreatments + " systemic treatments")
                    .addUndeterminedGeneralMessages("Nr of systemic treatments with PD")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient did not receive at least " + minSystemicTreatments + " systemic treatments")
                .addFailGeneralMessages("Nr of systemic treatments with PD")
                .build();
    }
}
