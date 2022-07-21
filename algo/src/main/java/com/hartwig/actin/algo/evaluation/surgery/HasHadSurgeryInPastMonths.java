package com.hartwig.actin.algo.evaluation.surgery;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.Surgery;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;

public class HasHadSurgeryInPastMonths implements EvaluationFunction {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @NotNull
    private final LocalDate minDate;

    HasHadSurgeryInPastMonths(@NotNull final LocalDate minDate) {
        this.minDate = minDate;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        for (Surgery surgery : record.clinical().surgeries()) {
            if (minDate.isBefore(surgery.endDate())) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.PASS)
                        .addPassSpecificMessages("Patient has had surgery after " + DATE_FORMAT.format(minDate))
                        .addPassGeneralMessages("Recent surgery")
                        .build();
            }
        }

        boolean hasSurgeryWithUndeterminedDate = false;
        for (PriorTumorTreatment priorTumorTreatment : record.clinical().priorTumorTreatments()) {
            if (priorTumorTreatment.categories().contains(TreatmentCategory.SURGERY)) {
                Integer year = priorTumorTreatment.startYear();
                if (year == null) {
                    hasSurgeryWithUndeterminedDate = true;
                } else {
                    Integer month = priorTumorTreatment.startMonth();
                    boolean isAfterMinDate =
                            minDate.getYear() < year || (minDate.getYear() == year && month != null && minDate.getMonthValue() <= month);

                    if (isAfterMinDate) {
                        return EvaluationFactory.unrecoverable()
                                .result(EvaluationResult.PASS)
                                .addPassSpecificMessages("Patient has had surgery after " + DATE_FORMAT.format(minDate))
                                .addPassGeneralMessages("Recent surgery")
                                .build();
                    } else if (minDate.getYear() == year && month == null) {
                        hasSurgeryWithUndeterminedDate = true;
                    }
                }
            }
        }

        if (hasSurgeryWithUndeterminedDate) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Patient has had surgery but undetermined how long ago")
                    .addUndeterminedGeneralMessages("Recent surgery")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient has not received surgery in past nr of months")
                .addFailGeneralMessages("Recent surgery")
                .build();
    }
}
