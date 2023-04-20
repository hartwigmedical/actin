package com.hartwig.actin.algo.evaluation.bloodtransfusion;

import java.time.LocalDate;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.BloodTransfusion;

import org.jetbrains.annotations.NotNull;

public class HasHadRecentBloodTransfusion implements EvaluationFunction {

    @NotNull
    private final TransfusionProduct product;
    @NotNull
    private final LocalDate minDate;

    HasHadRecentBloodTransfusion(@NotNull final TransfusionProduct product, @NotNull final LocalDate minDate) {
        this.product = product;
        this.minDate = minDate;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        String productString = product.display().toLowerCase();

        for (BloodTransfusion transfusion : record.clinical().bloodTransfusions()) {
            if (transfusion.product().equalsIgnoreCase(product.display()) && minDate.isBefore(transfusion.date())) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.PASS)
                        .addPassSpecificMessages("Patient has received recent blood transfusion of product " + productString)
                        .addPassGeneralMessages("Has had recent blood transfusion " + productString)
                        .build();
            }
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient has not received recent blood transfusion of product " + productString)
                .addFailGeneralMessages("Has not had recent blood transfusion " + productString)
                .build();
    }
}
