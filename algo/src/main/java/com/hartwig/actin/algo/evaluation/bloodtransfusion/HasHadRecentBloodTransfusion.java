package com.hartwig.actin.algo.evaluation.bloodtransfusion;

import java.time.LocalDate;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.EvaluationFactory;
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
        for (BloodTransfusion transfusion : record.clinical().bloodTransfusions()) {
            if (transfusion.product().equals(product.display()) && minDate.isBefore(transfusion.date())) {
                return EvaluationFactory.create(EvaluationResult.PASS);
            }
        }

        return EvaluationFactory.create(EvaluationResult.FAIL);
    }
}
