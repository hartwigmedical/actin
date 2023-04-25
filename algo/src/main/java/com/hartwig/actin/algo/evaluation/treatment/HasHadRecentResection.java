package com.hartwig.actin.algo.evaluation.treatment;

import java.time.LocalDate;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.DateComparison;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;

public class HasHadRecentResection implements EvaluationFunction {

    static final String RESECTION_KEYWORD = "resection";

    @NotNull
    private final LocalDate minDate;

    HasHadRecentResection(@NotNull final LocalDate minDate) {
        this.minDate = minDate;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasHadResectionAfterMinDate = false;
        boolean hasHadResectionAfterMoreLenientMinDate = false;
        boolean mayHaveHadResectionAfterMinDate = false;

        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            Boolean isPastMinDate = DateComparison.isAfterDate(minDate, treatment.startYear(), treatment.startMonth());
            Boolean isPastMoreLenientMinDate =
                    DateComparison.isAfterDate(minDate.minusWeeks(2), treatment.startYear(), treatment.startMonth());
            boolean isResection = treatment.name().toLowerCase().contains(RESECTION_KEYWORD.toLowerCase());
            boolean isPotentialResection = treatment.categories().contains(TreatmentCategory.SURGERY) && treatment.name().isEmpty();

            if (isResection) {
                if (isPastMinDate == null) {
                    mayHaveHadResectionAfterMinDate = true;
                }

                if (isPastMinDate != null && isPastMinDate) {
                    hasHadResectionAfterMinDate = true;
                }

                if (isPastMoreLenientMinDate != null && isPastMoreLenientMinDate) {
                    hasHadResectionAfterMoreLenientMinDate = true;
                }
            }

            if (isPastMinDate != null && isPastMinDate && isPotentialResection) {
                mayHaveHadResectionAfterMinDate = true;
            }
        }

        if (hasHadResectionAfterMinDate) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has had a recent resection")
                    .addPassGeneralMessages("Has had recent resection")
                    .build();
        } else if (hasHadResectionAfterMoreLenientMinDate) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addWarnSpecificMessages("Patient has had a reasonably recent resection")
                    .addWarnGeneralMessages("Has had reasonably recent resection")
                    .build();
        } else if (mayHaveHadResectionAfterMinDate) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Patient may have had a recent resection")
                    .addUndeterminedGeneralMessages("Unknown if has had recent resection")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient has not had a recent resection")
                .addFailGeneralMessages("Has not had recent resection")
                .build();
    }
}