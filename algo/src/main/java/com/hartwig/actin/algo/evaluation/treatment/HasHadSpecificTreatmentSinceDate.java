package com.hartwig.actin.algo.evaluation.treatment;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HasHadSpecificTreatmentSinceDate implements EvaluationFunction {

    @NotNull
    private final String query;
    private final LocalDate minDate;

    HasHadSpecificTreatmentSinceDate(@NotNull String treatmentName, LocalDate minDate) {
        this.query = treatmentName.toLowerCase();
        this.minDate = minDate;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<PriorTumorTreatment> matchingTreatments = record.clinical().priorTumorTreatments().stream()
                .filter(treatment -> treatment.name().toLowerCase().contains(query))
                .collect(Collectors.toSet());

        if (matchingTreatments.stream().anyMatch(treatment -> treatmentSinceMinDate(treatment, false)))
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassGeneralMessages("Matching treatment since date")
                    .addPassSpecificMessages(String.format("Treatment matching '%s' administered since %s", query, Format.date(minDate)))
                    .build();
        else if (matchingTreatments.stream().anyMatch(treatment -> treatmentSinceMinDate(treatment, true))) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addWarnGeneralMessages("Matching treatment with unknown date")
                    .addWarnSpecificMessages(String.format("Treatment matching '%s' administered with unknown date", query))
                    .build();
        } else if (!matchingTreatments.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailGeneralMessages("Matching treatment with earlier date")
                    .addFailSpecificMessages(String.format("Treatment matching '%s' administered before %s", query, Format.date(minDate)))
                    .build();
        } else {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailGeneralMessages("No matching treatments found")
                    .addFailSpecificMessages(String.format("No treatments matching '%s' in prior tumor history", query))
                    .build();
        }
    }

    private boolean treatmentSinceMinDate(PriorTumorTreatment treatment, boolean includeUnknown) {
        return yearAndMonthSinceEndDate(treatment.stopYear(), treatment.stopMonth())
                .orElse(yearAndMonthSinceEndDate(treatment.startYear(), treatment.startMonth()).orElse(includeUnknown));
    }

    private Optional<Boolean> yearAndMonthSinceEndDate(@Nullable Integer nullableYear, @Nullable Integer nullableMonth) {
        return Optional.ofNullable(nullableYear).map(year -> {
            if (year > minDate.getYear()) {
                return true;
            } else if (year == minDate.getYear()) {
                return Optional.ofNullable(nullableMonth)
                        .map(month -> month >= minDate.getMonthValue())
                        .orElse(true);
            } else {
                return false;
            }
        });
    }
}
