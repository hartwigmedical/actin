package com.hartwig.actin.algo.evaluation.treatment;

import java.util.Optional;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.jetbrains.annotations.NotNull;

public class HasHadPDFollowingSomeSystemicTreatments implements EvaluationFunction {

    static final String PD_LABEL = "PD";

    private final int minSystemicTreatments;
    private final boolean mustBeRadiological;

    HasHadPDFollowingSomeSystemicTreatments(final int minSystemicTreatments, final boolean mustBeRadiological) {
        this.minSystemicTreatments = minSystemicTreatments;
        this.mustBeRadiological = mustBeRadiological;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        int minSystemicCount = SystemicTreatmentAnalyser.minSystemicTreatments(record.clinical().priorTumorTreatments());
        int maxSystemicCount = SystemicTreatmentAnalyser.maxSystemicTreatments(record.clinical().priorTumorTreatments());

        if (minSystemicCount >= minSystemicTreatments) {
            Optional<PriorTumorTreatment> lastTreatmentOption =
                    SystemicTreatmentAnalyser.lastSystemicTreatment(record.clinical().priorTumorTreatments());
            if (lastTreatmentOption.map(lastTreatment -> PD_LABEL.equalsIgnoreCase(lastTreatment.stopReason()) || PD_LABEL.equalsIgnoreCase(
                    lastTreatment.bestResponse())).orElse(false)) {
                if (!mustBeRadiological) {
                    return EvaluationFactory.pass(
                            "Patient received at least " + minSystemicTreatments + " systemic treatments ending with PD",
                            "Nr of systemic treatments");
                } else {
                    return EvaluationFactory.undetermined("Patient received at least " + minSystemicTreatments
                                    + " systemic treatments ending with PD, undetermined if there is now radiological progression",
                            "Radiological progression after treatments");
                }
            } else if (lastTreatmentOption.map(lastTreatment -> lastTreatment.stopReason() == null || lastTreatment.bestResponse() == null)
                    .orElse(true)) {
                return EvaluationFactory.undetermined(
                        "Patient received at least " + minSystemicTreatments + " systemic treatments but unclear PD status",
                        "Nr of systemic treatments with PD");
            } else {
                return EvaluationFactory.fail("Patient received at least " + minSystemicTreatments + " systemic treatments with no PD",
                        "No PD after systemic treatment");
            }
        } else if (maxSystemicCount >= minSystemicTreatments) {
            return EvaluationFactory.undetermined(
                    "Undetermined if patient received at least " + minSystemicTreatments + " systemic treatments",
                    "Nr of systemic treatments with PD");
        }

        return EvaluationFactory.fail("Patient did not receive at least " + minSystemicTreatments + " systemic treatments",
                "Nr of systemic treatments with PD");
    }
}
