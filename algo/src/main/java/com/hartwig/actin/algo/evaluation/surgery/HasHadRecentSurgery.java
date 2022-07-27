package com.hartwig.actin.algo.evaluation.surgery;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.Surgery;

import org.jetbrains.annotations.NotNull;

public class HasHadRecentSurgery implements EvaluationFunction {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @NotNull
    private final LocalDate minDate;

    HasHadRecentSurgery(@NotNull final LocalDate minDate) {
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

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient has not had surgery after " + DATE_FORMAT.format(minDate))
                .addFailGeneralMessages("No recent surgery")
                .build();
    }
}
