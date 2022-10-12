package com.hartwig.actin.algo.evaluation.bloodtransfusion;

import java.time.LocalDate;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.BloodTransfusion;

import org.jetbrains.annotations.NotNull;

public class RequiresRegularHematopoieticSupport implements EvaluationFunction {

    static final Set<String> HEMATOPOIETIC_MEDICATION_CATEGORIES = Sets.newHashSet();

    @NotNull
    private final LocalDate minDate;
    @NotNull
    private final LocalDate maxDate;

    static {
        HEMATOPOIETIC_MEDICATION_CATEGORIES.add("erythropoietic growth factor");
        HEMATOPOIETIC_MEDICATION_CATEGORIES.add("colony stimulating factor");
    }

    RequiresRegularHematopoieticSupport(@NotNull final LocalDate minDate, @NotNull final LocalDate maxDate) {
        this.minDate = minDate;
        this.maxDate = maxDate;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        for (BloodTransfusion transfusion : record.clinical().bloodTransfusions()) {
            if (transfusion.date().isAfter(minDate) && transfusion.date().isBefore(maxDate)) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.PASS)
                        .addPassSpecificMessages(
                                "Patient has had blood transfusion between " + Format.date(minDate) + " and " + Format.date(maxDate))
                        .addPassGeneralMessages("Hematopoietic support")
                        .build();
            }
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("Currently regular hematopoietic support cannot be determined")
                .addUndeterminedSpecificMessages("Undetermined hematopoietic support")
                .build();
    }
}
