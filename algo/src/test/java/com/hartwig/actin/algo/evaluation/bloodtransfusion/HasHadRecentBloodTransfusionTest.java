package com.hartwig.actin.algo.evaluation.bloodtransfusion;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.time.LocalDate;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.BloodTransfusion;
import com.hartwig.actin.clinical.datamodel.ImmutableBloodTransfusion;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasHadRecentBloodTransfusionTest {

    @Test
    public void canEvaluate() {
        LocalDate minDate = LocalDate.of(2020, 3, 30);
        HasHadRecentBloodTransfusion function = new HasHadRecentBloodTransfusion(TransfusionProduct.THROMBOCYTE, minDate);

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(BloodTransfusionTestFactory.withBloodTransfusions(Lists.newArrayList())));

        BloodTransfusion tooOld = create(TransfusionProduct.THROMBOCYTE, minDate.minusWeeks(4));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(BloodTransfusionTestFactory.withBloodTransfusion(tooOld)));

        BloodTransfusion wrongProduct = create(TransfusionProduct.ERYTHROCYTE, minDate.plusWeeks(2));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(BloodTransfusionTestFactory.withBloodTransfusion(wrongProduct)));

        BloodTransfusion correct = create(TransfusionProduct.THROMBOCYTE, minDate.plusWeeks(2));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(BloodTransfusionTestFactory.withBloodTransfusion(correct)));
    }

    @NotNull
    private static BloodTransfusion create(@NotNull TransfusionProduct product, @NotNull LocalDate date) {
        return ImmutableBloodTransfusion.builder().product(product.display()).date(date).build();
    }
}