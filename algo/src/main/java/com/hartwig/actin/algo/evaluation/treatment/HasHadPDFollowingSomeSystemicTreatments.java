package com.hartwig.actin.algo.evaluation.treatment;

import java.util.List;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.jetbrains.annotations.NotNull;

public class HasHadPDFollowingSomeSystemicTreatments implements EvaluationFunction {

    private final int minSystemicTreatments;
    private final boolean mustBeRadiological;

    HasHadPDFollowingSomeSystemicTreatments(final int minSystemicTreatments, final boolean mustBeRadiological) {
        this.minSystemicTreatments = minSystemicTreatments;
        this.mustBeRadiological = mustBeRadiological;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        List<PriorTumorTreatment> priorTumorTreatments = record.clinical().priorTumorTreatments();
        int minSystemicCount = SystemicTreatmentAnalyser.minSystemicTreatments(priorTumorTreatments);
        int maxSystemicCount = SystemicTreatmentAnalyser.maxSystemicTreatments(priorTumorTreatments);

        if (minSystemicCount >= minSystemicTreatments) {
            return SystemicTreatmentAnalyser.lastSystemicTreatment(priorTumorTreatments)
                    .flatMap(ProgressiveDiseaseFunctions::treatmentResultedInPDOption)
                    .map(treatmentResultedInPD -> {
                        if (treatmentResultedInPD) {
                            if (!mustBeRadiological) {
                                return EvaluationFactory.pass(
                                        "Patient received at least " + minSystemicTreatments + " systemic treatments ending with PD",
                                        "Has received " + minSystemicTreatments + " systemic treatments with PD");
                            } else {
                                return EvaluationFactory.undetermined("Patient received at least " + minSystemicTreatments
                                                + " systemic treatments ending with PD, undetermined if there is currently radiological progression",
                                        "Undetermined if currently there is radiological progression");
                            }
                        } else {
                            return EvaluationFactory.fail(
                                    "Patient received at least " + minSystemicTreatments + " systemic treatments with no PD",
                                    "At least " + minSystemicTreatments + " systemic treatments but not with PD");
                        }
                    })
                    .orElse(EvaluationFactory.undetermined(
                            "Patient received at least " + minSystemicTreatments + " systemic treatments but unclear PD status",
                            "At least " + minSystemicTreatments + " systemic treatments but undetermined if PD"));
        } else if (maxSystemicCount >= minSystemicTreatments) {
            return EvaluationFactory.undetermined(
                    "Undetermined if patient received at least " + minSystemicTreatments + " systemic treatments",
                    "Undetermined if at least " + minSystemicTreatments + " systemic treatments");
        }

        return EvaluationFactory.fail("Patient did not receive at least " + minSystemicTreatments + " systemic treatments",
                "Nr of systemic treatments with PD is less than " + minSystemicTreatments);
    }
}
