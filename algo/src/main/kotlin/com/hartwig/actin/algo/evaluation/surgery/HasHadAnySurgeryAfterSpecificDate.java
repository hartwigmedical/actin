package com.hartwig.actin.algo.evaluation.surgery;

import java.time.LocalDate;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.DateComparison;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.Surgery;
import com.hartwig.actin.clinical.datamodel.SurgeryStatus;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;

public class HasHadAnySurgeryAfterSpecificDate implements EvaluationFunction {

    @NotNull
    private final LocalDate minDate;
    @NotNull
    private final LocalDate evaluationDate;

    public HasHadAnySurgeryAfterSpecificDate(@NotNull final LocalDate minDate, @NotNull final LocalDate evaluationDate) {
        this.minDate = minDate;
        this.evaluationDate = evaluationDate;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasFinishedSurgeryBetweenMinAndEval = false;
        boolean hasUnexpectedSurgeryBetweenMinAndEval = false;
        boolean hasCancelledSurgeryBetweenMinAndEval = false;
        boolean hasPlannedSurgeryAfterEval = false;
        boolean hasUnexpectedSurgeryAfterEval = false;
        boolean hasCancelledSurgeryAfterEval = false;

        for (Surgery surgery : record.clinical().surgeries()) {
            if (minDate.isBefore(surgery.endDate())) {
                if (minDate.isBefore(surgery.endDate())) {
                    if (evaluationDate.isBefore(surgery.endDate())) {
                        if (surgery.status() == SurgeryStatus.CANCELLED) {
                            hasCancelledSurgeryAfterEval = true;
                        } else if (surgery.status() == SurgeryStatus.PLANNED) {
                            hasPlannedSurgeryAfterEval = true;
                        } else {
                            hasUnexpectedSurgeryAfterEval = true;
                        }
                    } else {
                        if (surgery.status() == SurgeryStatus.FINISHED) {
                            hasFinishedSurgeryBetweenMinAndEval = true;
                        } else if (surgery.status() == SurgeryStatus.CANCELLED) {
                            hasCancelledSurgeryBetweenMinAndEval = true;
                        } else {
                            hasUnexpectedSurgeryBetweenMinAndEval = true;
                        }
                    }
                }
            }
        }

        if (hasFinishedSurgeryBetweenMinAndEval || hasPlannedSurgeryAfterEval) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has had or will get surgery after " + Format.date(minDate))
                    .addPassGeneralMessages("Surgery after " + Format.date(minDate))
                    .build();
        } else if (hasUnexpectedSurgeryAfterEval || hasUnexpectedSurgeryBetweenMinAndEval) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addWarnSpecificMessages("Patient may have had or may get surgery after " + Format.date(minDate))
                    .addWarnGeneralMessages("Potential recent surgery")
                    .build();
        }

        boolean hasSurgeryWithUndeterminedDate = false;
        for (PriorTumorTreatment priorTumorTreatment : record.clinical().priorTumorTreatments()) {
            if (priorTumorTreatment.categories().contains(TreatmentCategory.SURGERY)) {
                Boolean isAfterMinDate =
                        DateComparison.isAfterDate(minDate, priorTumorTreatment.startYear(), priorTumorTreatment.startMonth());
                if (isAfterMinDate == null) {
                    hasSurgeryWithUndeterminedDate = true;
                } else if (isAfterMinDate) {
                    return EvaluationFactory.unrecoverable()
                            .result(EvaluationResult.PASS)
                            .addPassSpecificMessages("Patient has had surgery after " + Format.date(minDate))
                            .addPassGeneralMessages("Has had surgery after " + Format.date(minDate))
                            .build();
                }
            }
        }

        if (hasSurgeryWithUndeterminedDate) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Patient has had surgery but undetermined how long ago")
                    .addUndeterminedGeneralMessages("Undetermined if previous surgery is recent")
                    .build();
        }

        if (hasCancelledSurgeryAfterEval || hasCancelledSurgeryBetweenMinAndEval) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("Recent surgery got cancelled")
                    .addFailGeneralMessages("No recent surgery")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient has not received surgery in past nr of months")
                .addFailGeneralMessages("No recent surgery")
                .build();
    }
}
